package unet.jrtmp.amf;

import java.nio.ByteBuffer;
import java.util.*;

public class AMF0 {

    public static void encode(ByteBuffer out, Object value){
        Types type = Types.getType(value);
        out.put((byte) type.value);

        switch(type){
            case NUMBER:
                if(value instanceof Double){
                    out.putLong(Double.doubleToLongBits((Double) value));

                }else{ // this coverts int also
                    out.putLong(Double.doubleToLongBits(Double.valueOf(value.toString())));
                }
                return;

            case BOOLEAN:
                out.put((byte) ((Boolean) value ? 0x01 : 0x00));
                return;

            case STRING: {
                    byte[] bytes = ((String) value).getBytes();
                    out.putShort((short) bytes.length);
                    out.put(bytes);
                    return;
                }

            case NULL:
                return;

            case MAP:
                out.putInt(0);
                // no break; remaining processing same as OBJECT

            case OBJECT: {
                    Map<String, Object> map = (Map) value;
                    for(Map.Entry<String, Object> entry : map.entrySet()){
                        byte[] bytes = entry.getKey().getBytes();
                        out.putShort((short) bytes.length);
                        out.put(bytes);

                        encode(out, entry.getValue());
                    }
                    out.put(new byte[]{ 0x00, 0x00, 0x09 });
                    return;
                }

            case ARRAY:
                Object[] array = (Object[]) value;
                out.putInt(array.length);
                for(Object o : array){
                    encode(out, o);
                }
                return;

            case DATE:
                long time = ((Date) value).getTime();
                out.putLong(Double.doubleToLongBits(time));
                out.putShort((short) 0);
                return;

            default:
                throw new RuntimeException("unexpected type: " + type);
        }
    }

    public static void encode(ByteBuffer out, List<Object> values){
        for(Object value : values){
            encode(out, value);
        }
    }

    public static List<Object> decodeAll(ByteBuffer payload){
        List<Object> result = new ArrayList<>();

        while(payload.hasRemaining()){
            result.add(decode(Types.valueToEnum(payload.get()), payload));
        }

        for(Object r : result){
            if(r == null){
                continue;
            }

            System.out.println(r.toString());
        }

        System.out.println();
        System.out.println();
        System.out.println();

        return result;
    }

    private static Object decode(Types type, ByteBuffer payload){
        switch(type){
            case NUMBER: { //NUMBER
                    return payload.getDouble();//Double.longBitsToDouble(payload.getLong()); //readDouble???
                }

            case BOOLEAN: { //BOOLEAN
                    return payload.get() != 0;
                }

            case STRING: { //STRING
                    byte[] str = new byte[payload.getShort()];
                    payload.get(str);
                    return new String(str);
                }

            case OBJECT, MAP: { //OBJECT
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

                        short size = payload.getShort();
                        if(size == 0){
                            if(payload.get() == 0x09){
                                //payload.position((payload.remaining() > 2) ? payload.position()+3 : payload.remaining());
                                break;
                            }
                        }

                        if(count > 0 && i++ == count){
                            break;
                        }

                        byte[] str = new byte[size];
                        payload.get(str);

                        map.put(new String(str), decode(Types.valueToEnum(payload.get()), payload));
                    }

                    return map;
                }

            case ARRAY: { //ARRAY
                    Object[] array = new Object[payload.getInt()];
                    for(int i = 0; i < array.length; i++){
                        array[i] = decode(Types.valueToEnum(payload.get()), payload);
                    }
                    return array;
                }

            case DATE: { //DATE
                    long dateValue = payload.getLong();
                    payload.getShort(); // consume the timezone
                    return new Date((long) Double.longBitsToDouble(dateValue));
                }

            case LONG_STRING: { //LONG STRING
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
        MAP(0x08),
        ARRAY(0x0A),
        DATE(0x0B),
        LONG_STRING(0x0C),
        //XML_DOCUMENT(0x0F),
        //TYPED_OBJECT(0x10),
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
