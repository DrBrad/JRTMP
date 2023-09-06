package unet.jrtmp.packets;

import static unet.jrtmp.packets.TSPacketManager.TS_PACKET_SIZE;

public class TSPacket extends Packet {

    private byte[] raw = new byte[TS_PACKET_SIZE];

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

        raw[0] = 0x47;  // Sync byte
        raw[1] = (byte) ((pid >> 8) & 0xFF);  // PID high byte
        raw[2] = (byte) (pid & 0xFF);         // PID low byte

        // Adaptation field control, set to 0x01 for no adaptation field
        raw[3] = (byte) 0x01;

        System.arraycopy(buffer, 0, raw, 4, TS_PACKET_SIZE-4);
    }

    @Override
    public byte[] getRaw(){
        return raw;
    }
}
