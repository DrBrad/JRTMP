package unet.jrtmp.stream;

import unet.jrtmp.amf.AMF0;
import unet.jrtmp.rtmp.messages.AudioMessage;
import unet.jrtmp.rtmp.messages.RtmpMediaMessage;
import unet.jrtmp.rtmp.messages.RtmpMessage;
import unet.jrtmp.rtmp.messages.VideoMessage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.Channel;
import java.util.*;

public class Stream {

    /*
    private static byte[] flvHeader = new byte[]{ 0x46, 0x4C, 0x56, 0x01, 0x05, 00, 00, 00, 0x09 };
    private Map<String, Object> metadata;
    private Channel publisher;
    private VideoMessage avcDecoderConfigurationRecord;
    private AudioMessage aacAudioSpecificConfig;
    private Set<Channel> subscribers, httpFLvSubscribers;
    private List<RtmpMediaMessage> content;
    private StreamName streamName;
    private int videoTimestamp, audioTimestamp, obsTimeStamp;
    private FileOutputStream flvout;
    private boolean flvHeadAndMetadataWritten = false;

    public Stream(StreamName streamName){
        subscribers = new LinkedHashSet<>();
        httpFLvSubscribers = new LinkedHashSet<>();
        content = new ArrayList<>();
        this.streamName = streamName;

        if(MyLiveConfig.INSTANCE.isSaveFlvFile()){
            createFileStream();
        }
    }

    public synchronized void addContent(RtmpMediaMessage message){
        if(streamName.isObsClient()){
            handleObsStream(message);
        }else{
            handleNonObsStream(message);
        }

        if(message instanceof VideoMessage){
            VideoMessage vm=(VideoMessage)message;
            if(vm.isAVCDecoderConfigurationRecord()){
                //log.info("avcDecoderConfigurationRecord  ok");
                avcDecoderConfigurationRecord = vm;
            }

            if(vm.isH264KeyFrame()){
                //log.debug("video key frame in stream :{}", streamName);
                content.clear();
            }
        }

        if(message instanceof AudioMessage){
            AudioMessage am=(AudioMessage) message;
            if(am.isAACAudioSpecificConfig()){
                aacAudioSpecificConfig = am;
            }
        }


        content.add(message);
        if(MyLiveConfig.INSTANCE.isSaveFlvFile()){
            writeFlv(message);
        }
        broadCastToSubscribers(message);
    }

    private void handleNonObsStream(RtmpMediaMessage message){
        if(message instanceof VideoMessage){
            VideoMessage vm = (VideoMessage) message;
            if(vm.getTimestamp() != null){
                // we may encode as FMT1 ,so we need timestamp delta
                vm.setTimestampDelta(vm.getTimestamp() - videoTimestamp);
                videoTimestamp = vm.getTimestamp();
            }else if(vm.getTimestampDelta() != null){
                videoTimestamp += vm.getTimestampDelta();
                vm.setTimestamp(videoTimestamp);
            }
        }

        if(message instanceof AudioMessage){
            AudioMessage am = (AudioMessage) message;
            if(am.getTimestamp() != null){
                am.setTimestampDelta(am.getTimestamp() - audioTimestamp);
                audioTimestamp = am.getTimestamp();
            }else if(am.getTimestampDelta() != null){
                audioTimestamp += am.getTimestampDelta();
                am.setTimestamp(audioTimestamp);
            }
        }
    }

    private void handleObsStream(RtmpMediaMessage message){
        // OBS rtmp stream is different from FFMPEG
        // it's timestamp_delta is delta of last packet,not same type of last packet

        // flv only require an absolute timestamp
        // but rtmp client like vlc require a timestamp-delta,which is relative to last
        // same media packet.
        if(message.getTimestamp() != null){
            obsTimeStamp=message.getTimestamp();

        }else if(message.getTimestampDelta() != null){
            obsTimeStamp += message.getTimestampDelta();
        }

        message.setTimestamp(obsTimeStamp);
        if(message instanceof VideoMessage){
            message.setTimestampDelta(obsTimeStamp - videoTimestamp);
            videoTimestamp = obsTimeStamp;
        }
        if(message instanceof AudioMessage){
            message.setTimestampDelta(obsTimeStamp - audioTimestamp);
            audioTimestamp = obsTimeStamp;
        }
    }

    private byte[] encodeMediaAsFlvTagAndPrevTagSize(RtmpMediaMessage message){
        int tagType = message.getMessageType();
        byte[] data = message.raw();
        int dataSize = data.length;
        int timestamp = message.getTimestamp() & 0xffffff;
        int timestampExtended = ((message.getTimestamp() & 0xff000000) >> 24);

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

    private void writeFlv(RtmpMediaMessage message){
        if(flvout == null){
            return;
        }
        try{
            if(!flvHeadAndMetadataWritten){
                writeFlvHeaderAndMetadata();
                flvHeadAndMetadataWritten = true;
            }
            byte[] encodeMediaAsFlv = encodeMediaAsFlvTagAndPrevTagSize(message);
            flvout.write(encodeMediaAsFlv);
            flvout.flush();

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private byte[] encodeFlvHeaderAndMetadata(){
        ByteBuf encodeMetaData = encodeMetaData();
        ByteBuf buf = Unpooled.buffer();

        RtmpMediaMessage message = content.get(0);
        int timestamp = message.getTimestamp() & 0xffffff;
        int timestampExtended = ((message.getTimestamp() & 0xff000000) >> 24);

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

    private void writeFlvHeaderAndMetadata()throws IOException {
        byte[] encodeFlvHeaderAndMetadata = encodeFlvHeaderAndMetadata();
        flvout.write(encodeFlvHeaderAndMetadata);
        flvout.flush();

    }

    private ByteBuf encodeMetaData(){
        ByteBuf buffer = Unpooled.buffer();
        List<Object> meta = new ArrayList<>();
        meta.add("onMetaData");
        meta.add(metadata);
        log.info("Metadata:{}", metadata);
        AMF0.encode(buffer, meta);

        return buffer;
    }

    private void createFileStream(){
        File f = new File(MyLiveConfig.INSTANCE.getSaveFlVFilePath() + "/" + streamName.getApp() + "_" + streamName.getName());
        try{
            FileOutputStream fos = new FileOutputStream(f);

            flvout = fos;

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public synchronized void addSubscriber(Channel channel){
        subscribers.add(channel);
        avcDecoderConfigurationRecord.setTimestamp(content.get(0).getTimestamp());
        channel.writeAndFlush(avcDecoderConfigurationRecord);

        for(RtmpMessage message : content){
            channel.writeAndFlush(message);
        }
    }

    public synchronized void addHttpFlvSubscriber(Channel channel){
        httpFLvSubscribers.add(channel);

        // 1. write flv header and metaData
        byte[] meta = encodeFlvHeaderAndMetadata();
        channel.writeAndFlush(Unpooled.wrappedBuffer(meta));

        // 2. write avcDecoderConfigurationRecord
        avcDecoderConfigurationRecord.setTimestamp(content.get(0).getTimestamp());
        byte[] config = encodeMediaAsFlvTagAndPrevTagSize(avcDecoderConfigurationRecord);
        channel.writeAndFlush(Unpooled.wrappedBuffer(config));

        // 3. write aacAudioSpecificConfig
        if(aacAudioSpecificConfig != null){
            aacAudioSpecificConfig.setTimestamp(content.get(0).getTimestamp());
            byte[] aac = encodeMediaAsFlvTagAndPrevTagSize(aacAudioSpecificConfig);
            channel.writeAndFlush(Unpooled.wrappedBuffer(aac));
        }
        // 4. write content

        for(RtmpMediaMessage message : content){
            channel.writeAndFlush(Unpooled.wrappedBuffer(encodeMediaAsFlvTagAndPrevTagSize(message)));
        }

    }

    private synchronized void broadCastToSubscribers(RtmpMediaMessage message){
        Iterator<Channel> iterator = subscribers.iterator();
        while(iterator.hasNext()){
            Channel next = iterator.next();
            if(next.isActive()){
                next.writeAndFlush(message);
            }else{
                iterator.remove();
            }
        }

        if(!httpFLvSubscribers.isEmpty()){
            byte[] encoded = encodeMediaAsFlvTagAndPrevTagSize(message);

            Iterator<Channel> httpIte = httpFLvSubscribers.iterator();
            while(httpIte.hasNext()){
                Channel next = httpIte.next();
                ByteBuf wrappedBuffer = Unpooled.wrappedBuffer(encoded);
                if(next.isActive()){
                    next.writeAndFlush(wrappedBuffer);
                }else{
                    log.info("http channel :{} is not active remove", next);
                    httpIte.remove();
                }

            }
        }

    }

    public synchronized void sendEofToAllSubscriberAndClose(){
        if(MyLiveConfig.INSTANCE.isSaveFlvFile() && flvout != null){
            try{
                flvout.flush();
                flvout.close();
            }catch(IOException e){
                e.printStackTrace();
            }
        }
        for(Channel sc : subscribers){
            sc.writeAndFlush(UserControlMessageEvent.streamEOF(Constants.DEFAULT_STREAM_ID))
                    .addListener(ChannelFutureListener.CLOSE);

        }

        for(Channel sc : httpFLvSubscribers){
            sc.writeAndFlush(DefaultLastHttpContent.EMPTY_LAST_CONTENT).addListener(ChannelFutureListener.CLOSE);

        }
    }
    */
}
