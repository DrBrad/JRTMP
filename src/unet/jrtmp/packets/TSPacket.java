package unet.jrtmp.packets;

import static unet.jrtmp.packets.TSPacketManager.TS_PACKET_SIZE;

public class TSPacket extends Packet {

    //private byte[] raw = new byte[TS_PACKET_SIZE];
    private boolean transportErrorIndicator = false, payloadUnitStartIndicator = true, transportPriority = false;
    private int PID, transportScramblingControl = 0, adaptationFieldControl = 1, continuityCounter;

    private byte[] payload;

    /*
    ==============================================================================
    | Sync | Err | Start | Priority | PID | Scrambling | Adaptation | Continuity |
    |------|-----|-------|----------|-----|------------|------------|------------|
    |  47  |  0  |   1   |    0     | PID |      00    |     00     |  Counter   |
    ==============================================================================
    */

    public TSPacket(byte[] payload){
        this.payload = payload;
        /*
        raw[0] = 0x47;
        raw[1] = 0x40;
        raw[2] = (byte) ((pid >> 8) & 0xFF);
        raw[3] = (byte) (pid & 0xFF);
        raw[4] = 0x30;
        raw[5] = (byte) (continuity & 0x0F);
        */

        /*
        raw[0] = 0x47;  // Sync byte
        raw[1] = (byte) ((pid >> 8) & 0xFF);  // PID high byte
        raw[2] = (byte) (pid & 0xFF);         // PID low byte

        // Adaptation field control, set to 0x01 for no adaptation field
        raw[3] = (byte) 0x01;

        System.arraycopy(buffer, 0, raw, 4, TS_PACKET_SIZE-4);
        */
    }

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
        return PID;
    }

    public void setPID(int PID){
        this.PID = PID;
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
        return continuityCounter;
    }

    public void setContinuityCounter(int continuityCounter){
        this.continuityCounter = continuityCounter;
    }

    @Override
    public byte[] getEncoded(){
        byte[] packet = new byte[TS_PACKET_SIZE];
        packet[0] = 0x47;
        packet[1] = (byte)(
                (transportErrorIndicator ? 0x80 : 0x00) |
                        (payloadUnitStartIndicator ? 0x40 : 0x00) |
                        (transportPriority ? 0x20 : 0x00) |
                        ((PID >> 8) & 0x1F)
        );
        packet[2] = (byte)(PID & 0xFF);
        packet[3] = (byte)(
                (transportScramblingControl << 6) |
                        (adaptationFieldControl << 4) |
                        continuityCounter
        );

        System.arraycopy(payload, 0, packet, 4, payload.length);

        return packet;
    }
}
