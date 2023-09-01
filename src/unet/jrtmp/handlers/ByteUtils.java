package unet.jrtmp.handlers;

public class ByteUtils {

    /*
    public static short readShort(byte[] buf, int pos){
        return (short) ((buf[pos] << 8)
                | (buf[pos+1] & 0xff));
    }

    public static int readInt(byte[] buf, int pos){
        return (((buf[pos] & 0xff) << 24)
                | ((buf[pos+1] & 0xff) << 16)
                | ((buf[pos+2] & 0xff) << 8)
                | (buf[pos+3] & 0xff));
    }

    public static long readLong(byte[] buf, int pos){
        return (((long)(buf[pos] & 0xff) << 56) |
                ((long)(buf[pos+1] & 0xff) << 48) |
                ((long)(buf[pos+2] & 0xff) << 40) |
                ((long)(buf[pos+3] & 0xff) << 32) |
                ((long)(buf[pos+4] & 0xff) << 24) |
                ((long)(buf[pos+5] & 0xff) << 16) |
                ((long)(buf[pos+6] & 0xff) <<  8) |
                ((long)(buf[pos+7] & 0xff)));
    }

    public static void writeInt(int value, byte[] buf, int pos){
        buf[pos] = ((byte) (0xff & (value >> 24)));
        buf[pos+1] = ((byte) (0xff & (value >> 16)));
        buf[pos+2] = ((byte) (0xff & (value >> 8)));
        buf[pos+3] = ((byte) (0xff & value));
    }
    */
}
