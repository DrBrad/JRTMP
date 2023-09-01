package unet.jrtmp.handlers;

import unet.jrtmp.rtmp.messages.RtmpMessage;
import unet.jrtmp.rtmp.messages.AudioMessage;
import unet.jrtmp.rtmp.messages.SetChunkSize;
import unet.jrtmp.rtmp.messages.UserControlMessageEvent;
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

    public void encode(RtmpMessage message)throws IOException {
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

    private void encodeAudio(AudioMessage message)throws IOException {
        if(firstAudio){
            encodeWithFmt0And3(message);
            firstAudio = true;
            return;
        }
        encodeWithFmt1(message, message.getTimestampDelta());
    }

    private void encodeVideo(VideoMessage message)throws IOException {
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

        byte[] payload = message.encodePayload();
        out.write((byte) ((timestampDelta >> 16) & 0xff));
        out.write((byte) ((timestampDelta >> 8) & 0xff));
        out.write((byte) (timestampDelta & 0xff));

        out.write((byte) ((payload.length >> 16) & 0xff));
        out.write((byte) ((payload.length >> 8) & 0xff));
        out.write((byte) (payload.length & 0xff));

        out.write(message.getMessageType());

        boolean fmt1Part = true;
        int position = 0;
        while(position < payload.length){
            int min = Math.min(chunkSize, payload.length-position);

            if(fmt1Part){
                out.write(payload, position, min);
                fmt1Part = false;
            }else{
                out.write(encodeFmtAndCsid(3, outboundCsid));
                out.write(payload, position, min);
            }

            position += min;
            //out.write(buffer);
            //out.write(payload, position, min);
            //buffer = Unpooled.buffer();
        }
    }

    private void encodeWithFmt0And3(RtmpMessage message)throws IOException {
        int csid = message.getOutboundCsid();

        byte[] basicHeader = encodeFmtAndCsid(0, csid);
        out.write(basicHeader);

        byte[] payload = message.encodePayload();

        long timestamp = getRelativeTime();
        boolean needExtraTime = false;
        if(timestamp >= 0XFFFFFF){
            needExtraTime = true;
            out.write((byte) 0xFF);
            out.write((byte) 0xFF);
            out.write((byte) 0xFF);

        }else{
            out.write((byte) ((timestamp >> 16) & 0xff));
            out.write((byte) ((timestamp >> 8) & 0xff));
            out.write((byte) (timestamp & 0xff));
        }
        // message length
        out.write((byte) ((payload.length >> 16) & 0xff));
        out.write((byte) ((payload.length >> 8) & 0xff));
        out.write((byte) (payload.length & 0xff));

        out.write(message.getMessageType());
        if(message instanceof UserControlMessageEvent){
            // message stream id in UserControlMessageEvent is always 0
            out.write(new byte[]{ 0, 0, 0, 0, 0 });

        }else{
            out.write(new byte[]{ 0, 0, 0, 5 });
        }

        if(needExtraTime){
            out.write((byte) (0xff & (timestamp >> 24)));
            out.write((byte) (0xff & (timestamp >> 16)));
            out.write((byte) (0xff & (timestamp >> 8)));
            out.write((byte) (0xff & timestamp));
        }
        // split by chunk size

        boolean fmt0Part = true;
        int position = 0;
        while(position < payload.length){
            int min = Math.min(chunkSize, payload.length-position);

            if(fmt0Part){
                out.write(payload, position, min);
                fmt0Part = false;
            }else{
                out.write(encodeFmtAndCsid(3, csid));
                out.write(payload, position, min);
            }

            position += min;
            //out.writeBytes(buffer);
            //buffer = Unpooled.buffer();
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
