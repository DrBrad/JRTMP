package unet.jrtmp.rtmp.messages;

public class RtmpObjectMessage extends RtmpMessage {

    //List<Object> body;

    @Override
    public int getOutboundCsid(){
        return 4;
    }

    @Override
    public int getMessageType(){
        return 19;
    }

    @Override
    public byte[] encodePayload(){
        return null;
    }
}
