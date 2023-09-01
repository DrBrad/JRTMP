package unet.jrtmp.rtmp.messages;

public class SetPeerBandwidth extends RtmpControlMessage {

    private int acknowledgementWindowSize, limitType;

    public SetPeerBandwidth(int acknowledgementWindowSize, int limitType){
        this.acknowledgementWindowSize = acknowledgementWindowSize;
        this.limitType = limitType;
    }

    @Override
    public int getMessageType(){
        return 6;
    }

    @Override
    public byte[] encodePayload(){
        return new byte[]{
                ((byte) (0xff & (acknowledgementWindowSize >> 24))),
                ((byte) (0xff & (acknowledgementWindowSize >> 16))),
                ((byte) (0xff & (acknowledgementWindowSize >> 8))),
                ((byte) (0xff & acknowledgementWindowSize)),
                (byte) limitType
        };
    }

    public int getAcknowledgementWindowSize(){
        return acknowledgementWindowSize;
    }

    public int getLimitType(){
        return limitType;
    }
}
