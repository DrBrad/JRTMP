package unet.jrtmp.rtmp.messages;

import unet.jrtmp.rtmp.RtmpMessage;

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
        /*
        ByteBuf buffer = Unpooled.buffer();
        AMF0.encode(buffer, data);
        return buffer;
        */
        return new byte[0];
    }
}
