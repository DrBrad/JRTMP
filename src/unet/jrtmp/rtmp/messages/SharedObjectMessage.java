package unet.jrtmp.rtmp.messages;

import unet.jrtmp.rtmp.RtmpMessage;

public class SharedObjectMessage extends RtmpMessage {

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
