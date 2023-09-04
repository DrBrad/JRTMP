package unet.jrtmp.packets;

import unet.jrtmp.amf.AMF0;
import unet.jrtmp.rtmp.messages.RtmpMediaMessage;
import unet.jrtmp.rtmp.messages.RtmpMessage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class FLVPacketManager extends PacketManager {

    public static final byte[] flvHeader = new byte[]{ 0x46, 0x4C, 0x56, 0x01, 0x05, 00, 00, 00, 0x09 };

    private boolean flvHeadAndMetadataWritten;
    private OutputStream out;

    public FLVPacketManager(){
        try{
            out = new FileOutputStream(new File("/home/brad/Downloads/test.ts"));
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void add(RtmpMediaMessage message){
        try{
            if(!flvHeadAndMetadataWritten){
                writeFlvHeaderAndMetadata(message);
                flvHeadAndMetadataWritten = true;
            }

            out.write(encodeMediaAsFlvTagAndPrevTagSize(message));
            out.flush();

        }catch(IOException e){
        }
    }

    @Override
    public void write(Packet packet){

    }

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
        */


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
        */

        return buffer.array();
    }

    private byte[] encodeFlvHeaderAndMetadata(RtmpMediaMessage message){
        /*
        ByteBuffer encodeMetaData = ByteBuffer.allocate(1024);

        List<Object> meta = new ArrayList<>();
        meta.add(metadata);
        AMF0.encode(buffer, meta);

        //ByteBuf encodeMetaData = encodeMetaData();
        //ByteBuf buf = Unpooled.buffer();

        ByteBuffer buffer = ByteBuffer.allocate(1024);

        //RtmpMediaMessage message = content.get(0);
        int timestamp = message.getTimestamp() & 0xffffff;
        int timestampExtended = ((message.getTimestamp() & 0xff000000) >> 24);

        buffer.put(flvHeader);
        buffer.putInt(0); // previousTagSize0

        int readableBytes = encodeMetaData.readableBytes();
        buffer.put((byte) 0x12); // script
        buffer.writeMedium(readableBytes);
        // make the first script tag timestamp same as the keyframe
        buffer.writeMedium(timestamp);
        buffer.put((byte) timestampExtended);
//		buf.writeInt(0); // timestamp + timestampExtended
        buffer.writeMedium(0);// streamid
        buffer.put(encodeMetaData);
        buffer.putInt(readableBytes + 11);

        //byte[] result = new byte[buf.readableBytes()];
        //buffer.readBytes(result);

        return buffer.array();
        */
        return null;
    }

    private void writeFlvHeaderAndMetadata(RtmpMediaMessage message)throws IOException {
        out.write(encodeFlvHeaderAndMetadata(message));
        out.flush();
    }
}
