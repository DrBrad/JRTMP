package unet.jrtmp.packets.mpegts;

import unet.jrtmp.packets.Packet;

import static unet.jrtmp.packets.mpegts.TSPacketManager.TS_PACKET_SIZE;

public class TSPacket extends Packet {

    //private byte[] raw = new byte[TS_PACKET_SIZE];
    protected boolean transportErrorIndicator = false, payloadUnitStartIndicator = true, transportPriority = false;
    protected int pid, continuity, transportScramblingControl = 0, adaptationFieldControl = 1;

    /*
    ==============================================================================
    | Sync | Err | Start | Priority | PID | Scrambling | Adaptation | Continuity |
    |------|-----|-------|----------|-----|------------|------------|------------|
    |  47  |  0  |   1   |    0     | PID |      00    |     00     |  Counter   |
    ==============================================================================
    */

    public boolean isTransportErrorIndicator(){
        return transportErrorIndicator;
    }

    public void setTransportErrorIndicator(boolean transportErrorIndicator){
        this.transportErrorIndicator = transportErrorIndicator;
    }

    public boolean isPayloadUnitStartIndicator(){
        return payloadUnitStartIndicator;
    }

    public void setPayloadUnitStartIndicator(boolean payloadUnitStartIndicator){
        this.payloadUnitStartIndicator = payloadUnitStartIndicator;
    }

    public boolean isTransportPriority(){
        return transportPriority;
    }

    public void setTransportPriority(boolean transportPriority){
        this.transportPriority = transportPriority;
    }

    public int getPID(){
        return pid;
    }

    public void setPID(int PID){
        this.pid = PID;
    }

    public int getTransportScramblingControl(){
        return transportScramblingControl;
    }

    public void setTransportScramblingControl(int transportScramblingControl){
        this.transportScramblingControl = transportScramblingControl;
    }

    public int getAdaptationFieldControl(){
        return adaptationFieldControl;
    }

    public void setAdaptationFieldControl(int adaptationFieldControl){
        this.adaptationFieldControl = adaptationFieldControl;
    }

    public int getContinuityCounter(){
        return continuity;
    }

    public void setContinuityCounter(int continuityCounter){
        this.continuity = continuityCounter;
    }

    @Override
    public byte[] getEncoded(){
        byte[] packet = new byte[TS_PACKET_SIZE];
        packet[0] = 0x47;
        packet[1] = (byte)(
                (transportErrorIndicator ? 0x80 : 0x00) |
                        (payloadUnitStartIndicator ? 0x40 : 0x00) |
                        (transportPriority ? 0x20 : 0x00) |
                        ((pid >> 8) & 0x1F)
        );
        packet[2] = (byte)(pid & 0xFF);
        packet[3] = (byte)(
                (transportScramblingControl << 6) |
                        (adaptationFieldControl << 4) |
                        continuity
        );

        return packet;
    }
}
