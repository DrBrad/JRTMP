package unet.jrtmp.rtmp.messages;

import tv.flixbox.server.rtmp.RtmpMessage;

import java.util.List;

public class RtmpCommandMessage extends RtmpMessage {

    private List<Object> command;

    public RtmpCommandMessage(List<Object> command){
        this.command = command;
    }

    @Override
    public int getOutboundCsid(){
        return 3;
    }

    @Override
    public int getMessageType(){
        return 20;
    }

    @Override
    public byte[] encodePayload(){
        /*
        ByteBuf buffer = Unpooled.buffer();
		AMF0.encode(buffer, command);
		return buffer;
        */
        return new byte[0];
    }
}
