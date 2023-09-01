package unet.jrtmp.amf;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

public class AMF0 {

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

    /*
    public AMF0(byte[] buf){
        this.buf = buf;
    }
    */

    public static List<Object> decodeAll(ByteBuffer payload){
        //ByteBuffer buffer = ByteBuffer.wrap(buf);
        //buffer.order(ByteOrder.BIG_ENDIAN);
        List<Object> result = new ArrayList<>();

        while(payload.hasRemaining()){
            result.add(decode(Types.valueToEnum(payload.get()), payload));
        }

        return result;
    }

    private static Object decode(Types type, ByteBuffer payload){
        switch(type){
            case NUMBER: { //NUMBER
                    //double d = buffer.getDouble();
                    double d = Double.longBitsToDouble(payload.getLong()); //readDouble???
                    System.out.println("NUMBER  "+d);
                    return d;
                    //return buffer.getDouble();
                }

            case BOOLEAN: { //BOOLEAN
                    boolean b = payload.get() != 0;
                    System.out.println("BOOLEAN  "+b);
                    return b;
                    //return buffer.get() != 0;
                }

            case STRING: { //STRING
                    byte[] str = new byte[payload.getShort()];
                    payload.get(str);
                    System.out.println("STRING  "+new String(str)+"  "+str.length);
                    return new String(str);
                }

            case OBJECT, MAP: { //OBJECT
                System.out.println("OBJECT");
                    int count;
                    Map<String, Object> map;

                    if(type == Types.MAP){
                        count = payload.getInt(); // should always be 0
                        map = new LinkedHashMap<>();
                        if(count > 0){
                            //log.debug("non-zero size for MAP type: {}", count);
                        }
                    }else{
                        count = 0;
                        map = new AMF0Object();
                    }

                    int i = 0;
                    while(payload.hasRemaining()){
                        /*
                        byte[] endMarker = new byte[3];
                        buffer.get(endMarker);

                        if(Arrays.equals(endMarker, OBJECT_END_MARKER)){
                            break;
                        }

                        buffer.position(buffer.position()-3);
                        */

                        if(count > 0 && i++ == count){
                            break;
                        }

                        short size = payload.getShort();
                        if(size == 0){
                            if(payload.get() == 0x09){
                                break;
                            }
                        }

                        byte[] str = new byte[size];
                        payload.get(str);

                        byte tb = payload.get();
                        Types typ = Types.valueToEnum(tb);
                        System.out.println("KEY  "+new String(str));

                        map.put(new String(str), decode(typ, payload));
                    }
                    return map;
                }

            case ARRAY: { //ARRAY
                System.out.println("ARRAY");
                    Object[] array = new Object[payload.getInt()];
                    for(int i = 0; i < array.length; i++){
                        array[i] = decode(Types.valueToEnum(payload.get()), payload);
                    }
                    return array;
                }

            case DATE: { //DATE
                System.out.println("DATE");
                    long dateValue = payload.getLong();
                    payload.getShort(); // consume the timezone
                    return new Date((long) Double.longBitsToDouble(dateValue));
                }

            case LONG_STRING: { //LONG STRING
                System.out.println("LONG STRING");
                    byte[] str = new byte[payload.getInt()];
                    payload.get(str);
                    return new String(str);
                }

            case NULL, UNSUPPORTED: //UNSUPPORTED
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
                    *./
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

    private static String toString(Types type, Object value){
        StringBuilder sb = new StringBuilder();
        sb.append('[').append(type).append(" ");
        if(type == Types.ARRAY){
            sb.append(Arrays.toString((Object[]) value));
        }else{
            sb.append(value);
        }
        sb.append(']');
        return sb.toString();
    }


    public enum Types {

        NUMBER(0x00),
        BOOLEAN(0x01),
        STRING(0x02),
        OBJECT(0x03),
        NULL(0x05),
        //UNDEFINED(0x06),
        MAP(0x08), //ECMA
        ARRAY(0x0A), //STRICT ARRAY
        DATE(0x0B),
        LONG_STRING(0x0C),
        XML_DOCUMENT(0x0F),
        TYPED_OBJECT(0x10),
        UNSUPPORTED(0x0D);

        private int value;

        Types(int value){
            this.value = value;
        }

        public int intValue(){
            return value;
        }

        private static Types getType(Object value){
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

        public static Types valueToEnum(int value){
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

                //case 0x06:
                //    return UNDEFINED;

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
}
