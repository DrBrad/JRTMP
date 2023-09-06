package unet.jrtmp.packets.mpegts;

public class TSMediaPacket extends TSPacket {

    private byte[] payload;

    /*
    ==============================================================================
    | Sync | Err | Start | Priority | PID | Scrambling | Adaptation | Continuity |
    |------|-----|-------|----------|-----|------------|------------|------------|
    |  47  |  0  |   1   |    0     | PID |      00    |     00     |  Counter   |
    ==============================================================================
    */

    public TSMediaPacket(byte[] payload, int pid, int continuity){
        this.payload = payload;
        this.pid = pid;
        this.continuity = continuity;
    }

    @Override
    public byte[] getEncoded(){
        byte[] packet = super.getEncoded();
        System.arraycopy(payload, 0, packet, 4, payload.length);
        return packet;
    }
}
