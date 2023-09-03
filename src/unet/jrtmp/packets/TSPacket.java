package unet.jrtmp.packets;

public class TSPacket extends Packet {

    public static final int TS_PACKET_SIZE = 182;

    private byte[] raw = new byte[TS_PACKET_SIZE+6];

    //private Status status;

    public TSPacket(byte[] buffer, int pid, int continuity){
        raw[0] = 0x47;
        raw[1] = 0x40;
        raw[2] = (byte) ((pid >> 8) & 0xFF);
        raw[3] = (byte) (pid & 0xFF);
        raw[4] = 0x30;
        raw[5] = (byte) (continuity & 0x0F);

        System.arraycopy(buffer, 0, raw, 6, TS_PACKET_SIZE);
    }

    /*
    public Status getStatus(){
        return status;
    }
    */

    @Override
    public byte[] getRaw(){
        return raw;
    }

    /*
    public enum Status {
        INCOMPLETE,
        COMPLETE
    }
    */
}
