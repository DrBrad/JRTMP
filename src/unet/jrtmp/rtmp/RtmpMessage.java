package unet.jrtmp.rtmp;

public abstract class RtmpMessage {

    int inboundHeaderLength, inboundBodyLength;

    public abstract int getOutboundCsid();

    public abstract int getMessageType();

    public abstract byte[] encodePayload();

    public void setInboundHeaderLength(int inboundHeaderLength){
        this.inboundHeaderLength = inboundHeaderLength;
    }

    public void setInboundBodyLength(int inboundBodyLength){
        this.inboundBodyLength = inboundBodyLength;
    }
}
