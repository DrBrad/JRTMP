package unet.jrtmp.packets;

public class TSPacket extends Packet {

    public static final int TS_PACKET_SIZE = 184;

    private byte[] raw = new byte[TS_PACKET_SIZE+4];

    /*
    ==============================================================================
    | Sync | Err | Start | Priority | PID | Scrambling | Adaptation | Continuity |
    |------|-----|-------|----------|-----|------------|------------|------------|
    |  47  |  0  |   1   |    0     | PID |      00    |     00     |  Counter   |
    ==============================================================================
    */

    public TSPacket(byte[] buffer, int pid, int continuity){
        /*
        raw[0] = 0x47;
        raw[1] = 0x40;
        raw[2] = (byte) ((pid >> 8) & 0xFF);
        raw[3] = (byte) (pid & 0xFF);
        raw[4] = 0x30;
        raw[5] = (byte) (continuity & 0x0F);
        */

        // Set the Sync byte (always 0x47)
        raw[0] = (byte) 0x47;

        // Set the Transport error indicator, Payload unit start indicator, and Transport priority bits
        raw[1] = (byte) 0x40;

        // Set the PID (13 bits)
        raw[1] |= (byte) ((pid >> 8) & 0x1F);
        raw[2] = (byte) (pid & 0xFF);

        // Set the Transport scrambling control, Adaptation field control bits
        raw[3] = (byte) 0x00;

        // Set the Continuity counter (4 bits)
        raw[3] |= (byte) (continuity & 0x0F);

        System.arraycopy(buffer, 0, raw, 4, TS_PACKET_SIZE);
    }

    @Override
    public byte[] getRaw(){
        return raw;
    }
}
