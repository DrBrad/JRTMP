package unet.jrtmp.stream;

import unet.jrtmp.packets.PacketManager;
import unet.jrtmp.packets.TSPacketManager;
import unet.jrtmp.rtmp.messages.AudioMessage;
import unet.jrtmp.rtmp.messages.RtmpMediaMessage;
import unet.jrtmp.rtmp.messages.VideoMessage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Stream {

    /**
    ** THIS STREAM IS STRICTLY FOR TS...
    **/

    private StreamName name;
    //private List<RtmpMediaMessage> content;
    private int videoTimestamp, audioTimestamp, obsTimeStamp;
    private Map<String, Object> metadata;

    private PacketManager packetManager;

    //private OutputStream out;

    //private ByteBuffer buffer;
    //private int continuityCounter;


    //EXTEND FOR VIDEO / AUDIO...

    public Stream(StreamName name){
        this.name = name;
        //content = new ArrayList<>();
        packetManager = new TSPacketManager();

        /*
        buffer = ByteBuffer.allocate(188);

        try{
            out = new FileOutputStream(new File("/home/brad/Downloads/test.ts"));
        }catch(IOException e){
            e.printStackTrace();
        }
        */
    }

    //public void setBitRate(double bitrate){
    //    this.bitrate = bitrate;
    //}

    public synchronized void add(RtmpMediaMessage message){
        if(name.isObsClient()){
            if(message.getTimestamp() != -1){
                obsTimeStamp = message.getTimestamp();

            }else if(message.getTimestampDelta() != -1){
                obsTimeStamp += message.getTimestampDelta();
            }

            message.setTimestamp(obsTimeStamp);
            if(message instanceof VideoMessage){
                message.setTimestampDelta(obsTimeStamp - videoTimestamp);
                videoTimestamp = obsTimeStamp;
            }else if(message instanceof AudioMessage){
                message.setTimestampDelta(obsTimeStamp - audioTimestamp);
                audioTimestamp = obsTimeStamp;
            }

        }else{
            if(message instanceof VideoMessage){
                VideoMessage vm = (VideoMessage) message;
                if(vm.getTimestamp() != -1){
                    // we may encode as FMT1 ,so we need timestamp delta
                    vm.setTimestampDelta(vm.getTimestamp() - videoTimestamp);
                    videoTimestamp = vm.getTimestamp();

                }else if(vm.getTimestampDelta() != -1){
                    videoTimestamp += vm.getTimestampDelta();
                    vm.setTimestamp(videoTimestamp);
                }
            }

            if(message instanceof AudioMessage){
                AudioMessage am = (AudioMessage) message;
                if(am.getTimestamp() != -1){
                    am.setTimestampDelta(am.getTimestamp() - audioTimestamp);
                    audioTimestamp = am.getTimestamp();

                }else if(am.getTimestampDelta() != -1){
                    audioTimestamp += am.getTimestampDelta();
                    am.setTimestamp(audioTimestamp);
                }
            }
        }

        packetManager.add(message.raw());
        //content.add(message);

        //TRY SAVING AS A FILE...

        //WE WILL LIKELY NEED TO DO SOME HEADER BS...

        /*
        try{
            System.out.println("BITRATE: "+metadata.get("videodatarate")+"  "+message.raw().length);

            if(message.raw().length > )

            out.write(createTSHeader());
            out.write(message.raw());
            out.flush();

        }catch(Exception e){//IO
            e.printStackTrace();
        }
        */

        //10 SECOND TS =



    }

    /*
    private static byte[] createTSHeader(){
        // Create a simplified TS header as a byte array
        byte[] tsHeader = {
                (byte) 0x47,  // Sync byte
                (byte) 0x40,  // Transport error indicator, payload unit start indicator
                (byte) 0x00, (byte) 0x10,  // PID (Packet Identifier) for video stream (0x0010)
                (byte) 0x30, (byte) 0x32,  // Transport scrambling control, adaptation field control
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xB0,  // Adaptation field length, discontinuity indicator
                (byte) 0x0D,  // Payload start indicator, payload length (13 bytes)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01,  // PCR flag, optional PCR values
                (byte) 0xE1,  // Stream type (0xE1 indicates H.264 video)
                (byte) 0x00, (byte) 0x00, (byte) 0x00  // Optional elementary stream specific data
        };

        return tsHeader;
    }
    */

    /*
    private byte[] encodeMediaAsFlvTagAndPrevTagSize(RtmpMediaMessage msg) {
        int tagType = msg.getMsgType();
        byte[] data = msg.raw();
        int dataSize = data.length;
        int timestamp = msg.getTimestamp() & 0xffffff;
        int timestampExtended = ((msg.getTimestamp() & 0xff000000) >> 24);

        ByteBuf buffer = Unpooled.buffer();

        buffer.writeByte(tagType);
        buffer.writeMedium(dataSize);
        buffer.writeMedium(timestamp);
        buffer.writeByte(timestampExtended);// timestampExtended
        buffer.writeMedium(0);// streamid
        buffer.writeBytes(data);
        buffer.writeInt(data.length + 11); // prevousTagSize

        byte[] r = new byte[buffer.readableBytes()];
        buffer.readBytes(r);

        return r;
    }

    private void writeFlv(RtmpMediaMessage msg) {
        if (flvout == null) {
            log.error("no flv file existed for stream : {}", streamName);
            return;
        }
        try {
            if (!flvHeadAndMetadataWritten) {
                writeFlvHeaderAndMetadata();
                flvHeadAndMetadataWritten = true;
            }
            byte[] encodeMediaAsFlv = encodeMediaAsFlvTagAndPrevTagSize(msg);
            flvout.write(encodeMediaAsFlv);
            flvout.flush();

        } catch (IOException e) {
            log.error("writting flv file failed , stream is :{}", streamName, e);
        }
    }

    private byte[] encodeFlvHeaderAndMetadata() {
        ByteBuf encodeMetaData = encodeMetaData();
        ByteBuf buf = Unpooled.buffer();

        RtmpMediaMessage msg = content.get(0);
        int timestamp = msg.getTimestamp() & 0xffffff;
        int timestampExtended = ((msg.getTimestamp() & 0xff000000) >> 24);

        buf.writeBytes(flvHeader);
        buf.writeInt(0); // previousTagSize0

        int readableBytes = encodeMetaData.readableBytes();
        buf.writeByte(0x12); // script
        buf.writeMedium(readableBytes);
        // make the first script tag timestamp same as the keyframe
        buf.writeMedium(timestamp);
        buf.writeByte(timestampExtended);
//		buf.writeInt(0); // timestamp + timestampExtended
        buf.writeMedium(0);// streamid
        buf.writeBytes(encodeMetaData);
        buf.writeInt(readableBytes + 11);

        byte[] result = new byte[buf.readableBytes()];
        buf.readBytes(result);

        return result;

    }

    private void writeFlvHeaderAndMetadata() throws IOException {
        byte[] encodeFlvHeaderAndMetadata = encodeFlvHeaderAndMetadata();
        flvout.write(encodeFlvHeaderAndMetadata);
        flvout.flush();

    }
    */










    public void setMetadata(Map<String, Object> metadata){
        this.metadata = metadata;
    }
}
