package unet.jrtmp.rtmp.messages;

import unet.jrtmp.amf.AMF0;

import java.nio.ByteBuffer;
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
        ByteBuffer buffer = ByteBuffer.allocate(1024);
		AMF0.encode(buffer, command);
        byte[] b = new byte[buffer.position()];
        buffer.rewind();
        buffer.get(b);
		return b;
    }

    public List<Object> getCommands(){
        return command;
    }
}
