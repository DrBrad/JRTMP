package unet.jrtmp.rtmp.messages;

import tv.flixbox.server.rtmp.RtmpMessage;

public abstract class RtmpControlMessage extends RtmpMessage {

    @Override
    public int getOutboundCsid(){
        return 2;
    }
}
