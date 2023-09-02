package unet.jrtmp.rtmp.messages;

import unet.jrtmp.amf.AMF0;

import java.nio.ByteBuffer;
import java.util.List;

public class RtmpDataMessage extends RtmpMessage {

    private List<Object> data;

    public RtmpDataMessage(List<Object> data){
        this.data = data;
    }

    @Override
    public int getOutboundCsid(){
        return 3;
    }

    @Override
    public int getMessageType(){
        return 18;
    }

    @Override
    public byte[] encodePayload(){
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        AMF0.encode(buffer, data);
        byte[] b = new byte[buffer.position()];
        buffer.rewind();
        buffer.get(b);
        return new byte[0];
    }

    public List<Object> getData(){
        return data;
    }
}
