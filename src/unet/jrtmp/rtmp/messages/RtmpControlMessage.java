package unet.jrtmp.rtmp.messages;

import unet.jrtmp.rtmp.RtmpMessage;

public abstract class RtmpControlMessage extends RtmpMessage {

    @Override
    public int getOutboundCsid(){
        return 2;
    }
}
