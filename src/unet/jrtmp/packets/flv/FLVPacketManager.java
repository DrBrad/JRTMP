package unet.jrtmp.packets.flv;

import unet.jrtmp.amf.AMF0;
import unet.jrtmp.packets.Packet;
import unet.jrtmp.packets.PacketManager;
import unet.jrtmp.rtmp.messages.AudioMessage;
import unet.jrtmp.rtmp.messages.RtmpMediaMessage;
import unet.jrtmp.rtmp.messages.RtmpMessage;
import unet.jrtmp.rtmp.messages.VideoMessage;
import unet.jrtmp.stream.Stream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FLVPacketManager extends PacketManager {

    public static final byte[] FLV_HEADER = new byte[]{ 0x46, 0x4C, 0x56, 0x01, 0x05, 0x00, 0x00, 0x00, 0x09 };

    private boolean flvHeadAndMetadataWritten;
    private OutputStream out;

    private Stream stream;

    public FLVPacketManager(Stream stream){
        this.stream = stream;
        try{
            out = new FileOutputStream(new File("/home/brad/Downloads/test.flv"));
            //out.write(FLV_HEADER);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void add(RtmpMediaMessage message){
        try{


            if(!flvHeadAndMetadataWritten){
                encodeFlvHeaderAndMetadata(message);
                flvHeadAndMetadataWritten = true;
            }


            out.write(encodeFLVHeader(message));

            out.flush();


            /*
            if(!flvHeadAndMetadataWritten){
                writeFlvHeaderAndMetadata(message);
                flvHeadAndMetadataWritten = true;
            }

            out.write(encodeMediaAsFlvTagAndPrevTagSize(message));
            out.flush();
            */

        }catch(IOException e){
        }
    }

    @Override
    public void write(Packet packet){

    }

    /*
    private void writeVideoTag(byte[] videoPayload, int timestamp) throws IOException {
        // FLV video tag header (11 bytes)
        byte[] videoTagHeader = {
                0x09, // Tag type (video)
                0x00, 0x00, 0x00, 0x00, // Data size (will be updated later)
                0x00, 0x00, 0x00, // Timestamp (milliseconds, will be updated)
                0x00, 0x00, 0x00 // Stream ID
        };

        // Update the data size in the tag header
        int dataSize = videoPayload.length + 5; // Video data size + 5 bytes for additional info
        videoTagHeader[1] = (byte) ((dataSize >> 16) & 0xFF);
        videoTagHeader[2] = (byte) ((dataSize >> 8) & 0xFF);
        videoTagHeader[3] = (byte) (dataSize & 0xFF);

        // Update the timestamp (you'll need to provide a proper timestamp)
        //long timestamp = 0; // Replace with the correct timestamp
        videoTagHeader[4] = (byte) ((timestamp >> 16) & 0xFF);
        videoTagHeader[5] = (byte) ((timestamp >> 8) & 0xFF);
        videoTagHeader[6] = (byte) (timestamp & 0xFF);

        // Write the video tag header
        out.write(videoTagHeader);

        // Write the video payload
        out.write(videoPayload);
    }

    private void writeAudioTag(byte[] audioPayload, int timestamp) throws IOException {
        // FLV audio tag header (11 bytes)
        byte[] audioTagHeader = {
                0x08, // Tag type (audio)
                0x00, 0x00, 0x00, 0x00, // Data size (will be updated later)
                0x00, 0x00, 0x00, // Timestamp (milliseconds, will be updated)
                0x00, 0x00, 0x00 // Stream ID
        };

        // Update the data size in the tag header
        int dataSize = audioPayload.length + 2; // Audio data size + 2 bytes for additional info
        audioTagHeader[1] = (byte) ((dataSize >> 16) & 0xFF);
        audioTagHeader[2] = (byte) ((dataSize >> 8) & 0xFF);
        audioTagHeader[3] = (byte) (dataSize & 0xFF);

        // Update the timestamp (you'll need to provide a proper timestamp)
        //long timestamp = 0; // Replace with the correct timestamp
        audioTagHeader[4] = (byte) ((timestamp >> 16) & 0xFF);
        audioTagHeader[5] = (byte) ((timestamp >> 8) & 0xFF);
        audioTagHeader[6] = (byte) (timestamp & 0xFF);

        // Write the audio tag header
        out.write(audioTagHeader);

        // Write the audio payload
        out.write(audioPayload);
    }
    */


    private byte[] encodeFlvHeaderAndMetadata(RtmpMediaMessage message){
        ByteBuffer encodeMetaData = ByteBuffer.allocate(4096);

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        List<Object> meta = new ArrayList<>();
        meta.add("onMetaData");
        meta.add(stream.getMetadata());
        AMF0.encode(encodeMetaData, meta);

        //RtmpMediaMessage message = content.get(0);
        int timestamp = message.getTimestamp() & 0xffffff;
        int timestampExtended = ((message.getTimestamp() & 0xff000000) >> 24);

        buffer.put(FLV_HEADER);
        buffer.putInt(0); // previousTagSize0

        buffer.put((byte) 0x12); // script
        buffer.put((byte) ((encodeMetaData.position() >> 16) & 0xff));
        buffer.put((byte) ((encodeMetaData.position() >> 8) & 0xff));
        buffer.put((byte) (encodeMetaData.position() & 0xff));
        // make the first script tag timestamp same as the keyframe
        buffer.put((byte) ((timestamp >> 16) & 0xff));
        buffer.put((byte) ((timestamp >> 8) & 0xff));
        buffer.put((byte) (timestamp & 0xff));
        buffer.put((byte) timestampExtended);
//		buf.writeInt(0); // timestamp + timestampExtended
        buffer.put((byte) ((0 >> 16) & 0xff));
        buffer.put((byte) ((0 >> 8) & 0xff));
        buffer.put((byte) (0 & 0xff));

        byte[] b = new byte[buffer.position()];
        buffer.rewind();
        buffer.get(b);

        buffer.put(b);
        buffer.putInt(b.length + 11);

        //byte[] result = new byte[buf.readableBytes()];
        //buffer.readBytes(result);

        b = new byte[buffer.position()];
        buffer.rewind();
        buffer.get(b);

        System.out.println(buffer.position()+"   "+encodeMetaData.position());

        return b;
    }

    private byte[] encodeFLVHeader(RtmpMediaMessage message){
        byte[] r = new byte[message.raw().length+16];
        r[0] = (byte) message.getMessageType();

        r[1] = (byte) ((message.raw().length >> 16) & 0xff);
        r[2] = (byte) ((message.raw().length >> 8) & 0xff);
        r[3] = (byte) (message.raw().length & 0xff);

        r[4] = (byte) ((message.getTimestamp() >> 16) & 0xff);
        r[5] = (byte) ((message.getTimestamp() >> 8) & 0xff);
        r[6] = (byte) (message.getTimestamp() & 0xff);

        r[7] = (byte) ((message.getTimestamp() & 0xff000000) >> 24);

        r[8] = (byte) ((0 >> 16) & 0xff);
        r[9] = (byte) ((0 >> 8) & 0xff);
        r[10] = (byte) (0 & 0xff);

        System.arraycopy(message.raw(), 0, r, 11, message.raw().length);

        r[message.raw().length+12] = (byte) (0xff & (message.raw().length+11 >> 24));
        r[message.raw().length+13] = (byte) (0xff & (message.raw().length+11 >> 16));
        r[message.raw().length+14] = (byte) (0xff & (message.raw().length+11 >> 8));
        r[message.raw().length+15] = (byte) (0xff & message.raw().length+11);

        return r;
    }

    /*

    private byte[] encodeMediaAsFlvTagAndPrevTagSize(RtmpMediaMessage msg){
        int tagType = msg.getMessageType();
        byte[] data = msg.raw();
        int timestamp = msg.getTimestamp() & 0xffffff;
        int timestampExtended = ((msg.getTimestamp() & 0xff000000) >> 24);

        ByteBuffer buffer = ByteBuffer.allocate(data.length+15);
        buffer.put((byte) tagType);

        buffer.put((byte) ((data.length >> 16) & 0xff));
        buffer.put((byte) ((data.length >> 8) & 0xff));
        buffer.put((byte) (data.length & 0xff));

        buffer.put((byte) ((timestamp >> 16) & 0xff));
        buffer.put((byte) ((timestamp >> 8) & 0xff));
        buffer.put((byte) (timestamp & 0xff));

        buffer.put((byte) timestampExtended);

        buffer.put((byte) ((0 >> 16) & 0xff));
        buffer.put((byte) ((0 >> 8) & 0xff));
        buffer.put((byte) (0 & 0xff));

        buffer.put(data);

        buffer.putInt(data.length+11);

        /*
        byte[] r = new byte[data.length+15];
        r[0] = (byte) tagType;

        r[1] = (byte) ((data.length >> 16) & 0xff);
        r[2] = (byte) ((data.length >> 8) & 0xff);
        r[3] = (byte) (data.length & 0xff);

        r[4] = (byte) ((timestamp >> 16) & 0xff);
        r[5] = (byte) ((timestamp >> 8) & 0xff);
        r[6] = (byte) (timestamp & 0xff);

        r[7] = (byte) timestampExtended;

        r[8] = (byte) ((0 >> 16) & 0xff);
        r[9] = (byte) ((0 >> 8) & 0xff);
        r[10] = (byte) (0 & 0xff);

        System.arraycopy(data, 0, r, 11, data.length);

        r[data.length+12] = (byte) (0xff & (data.length+11 >> 24));
        r[data.length+13] = (byte) (0xff & (data.length+11 >> 16));
        r[data.length+14] = (byte) (0xff & (data.length+11 >> 8));
        r[data.length+15] = (byte) (0xff & data.length+11);
        *./


        /*
        ByteBuf buffer = Unpooled.buffer();

        buffer.writeByte(tagType); 1
        buffer.writeMedium(dataSize); 3
        buffer.writeMedium(timestamp); 3
        buffer.writeByte(timestampExtended); 1// timestampExtended
        buffer.writeMedium(0); 3// streamid
        buffer.writeBytes(data); 1
        buffer.writeInt(data.length + 11); 4// prevousTagSize

        byte[] r = new byte[buffer.readableBytes()];
        buffer.readBytes(r);
        *./

        return buffer.array();
    }
    */
    /*

    private byte[] encodeFlvHeaderAndMetadata(RtmpMediaMessage message){
        ByteBuffer encodeMetaData = ByteBuffer.allocate(1024);

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        List<Object> meta = new ArrayList<>();
        meta.add("onMetaData");
        meta.add(meta);
        AMF0.encode(encodeMetaData, meta);

        //RtmpMediaMessage message = content.get(0);
        int timestamp = message.getTimestamp() & 0xffffff;
        int timestampExtended = ((message.getTimestamp() & 0xff000000) >> 24);

        buffer.put(flvHeader);
        buffer.putInt(0); // previousTagSize0

        buffer.put((byte) 0x12); // script
        buffer.put((byte) ((encodeMetaData.position() >> 16) & 0xff));
        buffer.put((byte) ((encodeMetaData.position() >> 8) & 0xff));
        buffer.put((byte) (encodeMetaData.position() & 0xff));
        // make the first script tag timestamp same as the keyframe
        buffer.put((byte) ((timestamp >> 16) & 0xff));
        buffer.put((byte) ((timestamp >> 8) & 0xff));
        buffer.put((byte) (timestamp & 0xff));
        buffer.put((byte) timestampExtended);
//		buf.writeInt(0); // timestamp + timestampExtended
        buffer.put((byte) ((0 >> 16) & 0xff));
        buffer.put((byte) ((0 >> 8) & 0xff));
        buffer.put((byte) (0 & 0xff));
        buffer.put(encodeMetaData);
        buffer.putInt(encodeMetaData.position() + 11);

        //byte[] result = new byte[buf.readableBytes()];
        //buffer.readBytes(result);

        return buffer.array();
    }
    */

    /*
    private void writeFlvHeaderAndMetadata(RtmpMediaMessage message)throws IOException {
        out.write(encodeFlvHeaderAndMetadata(message));
        out.flush();
    }
    */
}
