package unet.jrtmp.rtmp.messages;

public class AudioMessage extends RtmpMediaMessage {

    private byte[] audioData;

    public AudioMessage(byte[] audioData){
        this.audioData = audioData;
    }

    @Override
    public int getOutboundCsid(){
        return 10;
    }

    @Override
    public int getMessageType(){
        return 8;
    }

    @Override
    public byte[] encodePayload(){
        return audioData;
    }

    @Override
    public byte[] raw(){
        return audioData;
    }

    public boolean isAACAudioSpecificConfig(){
        return audioData.length > 1 && audioData[1] == 0;
    }

    @Override
    public String toString(){
        return "";
    }
}
