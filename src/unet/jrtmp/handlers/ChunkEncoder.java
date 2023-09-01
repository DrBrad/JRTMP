package unet.jrtmp.handlers;

import unet.jrtmp.rtmp.RtmpMessage;
import unet.jrtmp.rtmp.messages.AudioMessage;
import unet.jrtmp.rtmp.messages.SetChunkSize;
import unet.jrtmp.rtmp.messages.VideoMessage;

import java.io.IOException;
import java.io.OutputStream;

public class ChunkEncoder {

    private int chunkSize = 128;
    private long timestampBegin = System.currentTimeMillis();
    private boolean firstVideo = true, firstAudio = true;

    private OutputStream out;

    public ChunkEncoder(OutputStream out){
        this.out = out;
    }

    public void encode(RtmpMessage message){
        if(message instanceof SetChunkSize){
            chunkSize = ((SetChunkSize) message).getChunkSize();
        }

        if(message instanceof AudioMessage){
            encodeAudio((AudioMessage) message);

        }else if(message instanceof VideoMessage){
            encodeVideo((VideoMessage) message);

        }else{
            encodeWithFmt0And3(message);
        }
    }

    private void encodeAudio(AudioMessage message){
        if(firstAudio){
            encodeWithFmt0And3(message);
            firstAudio = true;
            return;
        }
        encodeWithFmt1(message, message.getTimestampDelta());
    }

    private void encodeVideo(VideoMessage message){
        if(firstVideo){
            encodeWithFmt0And3(message);
            firstVideo = true;
            return;
        }
        encodeWithFmt1(message, message.getTimestampDelta());
    }

    private void encodeWithFmt1(RtmpMessage message, int timestampDelta)throws IOException {
        int outboundCsid = message.getOutboundCsid();

        out.write(encodeFmtAndCsid(1, outboundCsid));
        //buffer.writeBytes(encodeFmtAndCsid(1, outboundCsid));

        out.write(message.encodePayload());
        out.write(); //timestampDelta - Medium
        out.write(); //payload length - Medium
        out.write(message.getMessageType());

        //ByteBuf payload = msg.encodePayload();
        //buffer.writeMedium(timestampDelta);
        //buffer.writeMedium(payload.readableBytes());
        //buffer.writeByte(msg.getMsgType());

        boolean fmt1Part = true;
        while(payload.isReadable()){
            int min = Math.min(chunkSize, payload.readableBytes());

            if (fmt1Part) {
                buffer.writeBytes(payload, min);
                fmt1Part = false;
            } else {
                byte[] fmt3BasicHeader = encodeFmtAndCsid(Constants.CHUNK_FMT_3, outboundCsid);
                buffer.writeBytes(fmt3BasicHeader);
                buffer.writeBytes(payload, min);

            }
            out.writeBytes(buffer);
            buffer = Unpooled.buffer();
        }
    }

    private void encodeWithFmt0And3(RtmpMessage message){
        int csid = message.getOutboundCsid();

        byte[] basicHeader = encodeFmtAndCsid(0, csid);

        // as for control msg ,we always use 0 timestamp

        ByteBuf payload = message.encodePayload();
        int messageLength = payload.readableBytes();
        ByteBuf buffer = Unpooled.buffer();

        buffer.writeBytes(basicHeader);

        long timestamp = getRelativeTime();
        boolean needExtraTime = false;
        if (timestamp >= Constants.MAX_TIMESTAMP) {
            needExtraTime = true;
            buffer.writeMedium(Constants.MAX_TIMESTAMP);
        } else {
            buffer.writeMedium((int) timestamp);
        }
        // message length
        buffer.writeMedium(messageLength);

        buffer.writeByte(msg.getMsgType());
        if (msg instanceof UserControlMessageEvent) {
            // message stream id in UserControlMessageEvent is always 0
            buffer.writeIntLE(0);
        } else {
            buffer.writeIntLE(Constants.DEFAULT_STREAM_ID);
        }

        if (needExtraTime) {
            buffer.writeInt((int) (timestamp));
        }
        // split by chunk size

        boolean fmt0Part = true;
        while (payload.isReadable()) {
            int min = Math.min(chunkSize, payload.readableBytes());
            if (fmt0Part) {
                buffer.writeBytes(payload, min);
                fmt0Part = false;
            } else {
                byte[] fmt3BasicHeader = encodeFmtAndCsid(Constants.CHUNK_FMT_3, csid);
                buffer.writeBytes(fmt3BasicHeader);
                buffer.writeBytes(payload, min);

            }
            out.writeBytes(buffer);
            buffer = Unpooled.buffer();
        }
    }

    public long getRelativeTime(){
        return System.currentTimeMillis()-timestampBegin;
    }

    private static byte[] encodeFmtAndCsid(int fmt, int csid){
        if(csid <= 63){
            return new byte[]{
                    (byte) ((fmt << 6) + csid)
            };

        }else if(csid <= 320){
            return new byte[]{
                    (byte) (fmt << 6),
                    (byte) (csid - 64)
            };

        }else{
            return new byte[]{
                    (byte) ((fmt << 6) | 1),
                    (byte) ((csid - 64) & 0xff),
                    (byte) ((csid - 64) >> 8)
            };
        }
    }
}
