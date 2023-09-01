package unet.jrtmp.amf;

import java.nio.ByteBuffer;
import java.util.*;

import static unet.jrtmp.handlers.ByteUtils.*;

public class AMF0 {

    public enum Type {

        NUMBER(0x00),
        BOOLEAN(0x01),
        STRING(0x02),
        OBJECT(0x03),
        NULL(0x05),
        UNDEFINED(0x06),
        MAP(0x08),
        ARRAY(0x0A),
        DATE(0x0B),
        LONG_STRING(0x0C),
        UNSUPPORTED(0x0D);

        private final int value;

        Type(int value){
            this.value = value;
        }

        public int intValue(){
            return value;
        }

        private static Type getType(final Object value){
            if(value == null){
                return NULL;

            }else if(value instanceof String){
                return STRING;

            }else if(value instanceof Number){
                return NUMBER;

            }else if(value instanceof Boolean){
                return BOOLEAN;

            }else if(value instanceof AMF0Object){
                return OBJECT;

            }else if(value instanceof Map){
                return MAP;

            }else if(value instanceof Object[]){
                return ARRAY;

            }else if(value instanceof Date){
                return DATE;

            }else{
                throw new RuntimeException("unexpected type: " + value.getClass());
            }
        }

        public static Type valueToEnum(int value){
            switch(value){
                case 0x00:
                    return NUMBER;

                case 0x01:
                    return BOOLEAN;

                case 0x02:
                    return STRING;

                case 0x03:
                    return OBJECT;

                case 0x05:
                    return NULL;

                case 0x06:
                    return UNDEFINED;

                case 0x08:
                    return MAP;

                case 0x0A:
                    return ARRAY;

                case 0x0B:
                    return DATE;

                case 0x0C:
                    return LONG_STRING;

                case 0x0D:
                    return UNSUPPORTED;

                default:
                    throw new RuntimeException("unexpected type: " + value);
            }
        }
    }

    //private static final byte BOOLEAN_TRUE = 0x01, BOOLEAN_FALSE = 0x00;
    private static final byte[] OBJECT_END_MARKER = new byte[]{ 0x00, 0x00, 0x09 };

    /*
    public static void encode(OutputStream out, Object value)throws IOException {
        Type type = Type.getType(value);
        out.write((byte) type.value);

        switch(type){
            case NUMBER:
                if(value instanceof Double){
                    out.writeLong(Double.doubleToLongBits((Double) value));

                }else{ // this coverts int also
                    out.writeLong(Double.doubleToLongBits(Double.valueOf(value.toString())));
                }
                return;

            case BOOLEAN:
                out.write((Boolean) value ? BOOLEAN_TRUE : BOOLEAN_FALSE);
                return;

            case STRING:
                encodeString(out, (String) value);
                return;

            case NULL:
                return;

            case MAP:
                out.write(new byte[]{
                        (byte) (0xff & (0 >> 24)),
                        (byte) (0xff & (0 >> 16)),
                        (byte) (0xff & (0 >> 8)),
                        (byte) (0xff & 0)
                });
                // no break; remaining processing same as OBJECT

            case OBJECT:
                final Map<String, Object> map = (Map) value;
                for (final Map.Entry<String, Object> entry : map.entrySet()) {
                    encodeString(out, entry.getKey());
                    encode(out, entry.getValue());
                }
                out.writeBytes(OBJECT_END_MARKER);
                return;

            case ARRAY:
                final Object[] array = (Object[]) value;
                out.writeInt(array.length);
                for (Object o : array) {
                    encode(out, o);
                }
                return;

            case DATE:
                final long time = ((Date) value).getTime();
                out.writeLong(Double.doubleToLongBits(time));
                out.writeShort((short) 0);
                return;

            default:
                // ignoring other types client doesn't require for now
                throw new RuntimeException("unexpected type: " + type);
        }
    }


    private static void encodeString(OutputStream out, String value){
        byte[] bytes = value.getBytes(); // UTF-8 ?
        out.writeShort((short) bytes.length);
        out.writeBytes(bytes);
    }

    public static void encode(OutputStream out, List<Object> values){
        for(Object value : values){
            encode(out, value);
        }
    }

    */

    private byte[] buf;
    private int pos;

    public AMF0(byte[] buf){
        this.buf = buf;
    }

    public static List<Object> decodeAll(byte[] buf){
        ByteBuffer buffer = ByteBuffer.wrap(buf);
        List<Object> result = new ArrayList<>();

        while(buffer.remaining() > 0){
            result.add(buffer.get(), buffer);
        }


        /*
        while(pos > buf.length){
            result.add(decode());
        }

        System.out.println(buf.length+"  "+new String(buf)+"  "+result.size());
        */

        return result;
    }

    private static Object decode(byte type, ByteBuffer buffer){
        switch(type){
            case 0x00: //NUMBER
                return buffer.getDouble();

            case 0x01: //BOOLEAN
                return buffer.get() != 0;

            case 0x02: { //STRING
                    int length = buffer.getShort() & 0xFFFF; // Convert to unsigned short
                    byte[] str = new byte[length];
                    buffer.get(str);
                    return new String(str);
                }

            case 0x03: { //OBJECT
                    int count;
                    Map<String, Object> map;
                    if(type == 0x08){
                        count = buffer.getInt(); // should always be 0
                        map = new LinkedHashMap<>();
                        if(count > 0){
                            //log.debug("non-zero size for MAP type: {}", count);
                        }
                    }else{
                        count = 0;
                        map = new AMF0Object();
                    }

                    int i = 0;
                    byte[] endMarker = new byte[3];
                    while(buffer.remaining() > 0){
                        buffer.get(endMarker);
                        if(Arrays.equals(endMarker, OBJECT_END_MARKER)){
                            buffer.position(buffer.position()+3);
                            //log.debug("end MAP / OBJECT, found object end marker [000009]");
                            break;
                        }
                        if(count > 0 && i++ == count){
                            //log.debug("stopping map decode after reaching count: {}", count);
                            break;
                        }
                        map.put(decodeString(in), decode(in));
                    }
                    return map;
                }

            case 0x0A: { //ARRAY
                    int arraySize = in.readInt();
                    Object[] array = new Object[arraySize];
                    for(int i = 0; i < arraySize; i++){
                        array[i] = decode(in);
                    }
                    return array;
                }

            case 0x0B: { //DATE
                    long dateValue = buffer.getLong();
                    buffer.getShort(); // consume the timezone
                    return new Date((long) Double.longBitsToDouble(dateValue));
                }

            case 0x0C: { //LONG STRING
                    int stringSize = buffer.getInt();
                    byte[] str = new byte[stringSize];
                    buffer.get(str);
                    return new String(str);
                }

            case 0x0D, 0x05: //UNSUPPORTED
                return null;

            // Add more cases for other AMF0 types as needed
            default:
                throw new UnsupportedOperationException("Unsupported AMF0 type: "+type);
        }
    }

    /*
    public static Object decode(byte[] in){
        final Type type = Type.valueToEnum(in.read());
        final Object value = decode(in, type);
        return value;
    }

    public static List<Object> decodeAll(byte[] in){
        List<Object> result = new ArrayList<>();

        while(in.available() > 0){
            Object decode = decode(in);
            result.add(decode);
        }
        return result;

    }
    *./

    private Object decode(){
        pos++;
        switch(Type.valueToEnum(buf[pos-1])){
            case NUMBER:
                pos += 8;
                return Double.longBitsToDouble(readLong(buf, pos-8));

            case BOOLEAN:
                pos++;
                return buf[pos-1] == BOOLEAN_TRUE;

            case STRING: {
                    int length = readShort(buf, pos);
                    pos += 2+length;
                    return new String(buf, pos-length, length);
                }

            case ARRAY: {
                    int arraySize = readInt(buf, pos);
                    pos += 4;
                    Object[] array = new Object[arraySize];

                    for(int i = 0; i < arraySize; i++){
                        array[i] = decode();
                    }
                    return array;
                }

            case MAP:

            case OBJECT: {
                    int count;
                    Map<String, Object> map;
                    /*
                    if(type == Type.MAP){
                        count = in.readInt(); // should always be 0
                        map = new LinkedHashMap<>();
                        //if(count > 0){
                        //}

                    }else{
                    }
                    */
                    count = 0;
                    map = new AMF0Object();

                    int i = 0;
                    byte[] endMarker = new byte[3];
                    while(pos < buf.length){
                        System.arraycopy(buf, pos, endMarker, 0, endMarker.length);
                        pos += 4;

                        if(Arrays.equals(endMarker, OBJECT_END_MARKER)){
                            pos += 3;
                            break;
                        }

                        if(count > 0 && i++ == count){
                            break;
                        }

                        int length = readShort(buf, pos);
                        pos += 2+length;
                        map.put(new String(buf, pos-length, length), decode());
                    }
                    return map;
                }

            case DATE: {
                    long dateValue = readLong(buf, pos);
                    readShort(buf, pos+8); // consume the timezone
                    pos += 10;
                    return new Date((long) Double.longBitsToDouble(dateValue));
                }

            case LONG_STRING: {
                    int length = readInt(buf, pos);
                    pos += 4+length;
                    return new String(buf, pos-length, length); // UTF-8 ?
                }

            case NULL, UNDEFINED, UNSUPPORTED:
                return null;

            default:
                throw new RuntimeException("unexpected type: ");
        }
    }
    */

    private static String toString(Type type, Object value){
        StringBuilder sb = new StringBuilder();
        sb.append('[').append(type).append(" ");
        if(type == Type.ARRAY){
            sb.append(Arrays.toString((Object[]) value));
        }else{
            sb.append(value);
        }
        sb.append(']');
        return sb.toString();
    }
}
