package unet.jrtmp.rtmp.messages;

public class VideoMessage extends RtmpMediaMessage {

    private byte[] videoData;

    public VideoMessage(byte[] videoData){
        this.videoData = videoData;
    }

    @Override
    public int getOutboundCsid(){
        return 12;
    }

    @Override
    public int getMessageType(){
        return 9;
    }

    @Override
    public byte[] encodePayload(){
        return videoData;
    }

    @Override
    public byte[] raw(){
        return videoData;
    }

    public boolean isH264KeyFrame(){
        return videoData.length > 1 && videoData[0] == 0x17;
    }

    public boolean isAVCDecoderConfigurationRecord(){
        return isH264KeyFrame() && videoData.length > 2 && videoData[1] == 0x00;
    }
}
