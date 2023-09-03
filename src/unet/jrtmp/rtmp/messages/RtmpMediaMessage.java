package unet.jrtmp.rtmp.messages;

public abstract class RtmpMediaMessage extends RtmpMessage {

    private int timestamp = -1, timestampDelta = -1;

    public abstract byte[] raw();

    public void setTimestamp(int timestamp){
        this.timestamp = timestamp;
    }

    public void setTimestampDelta(int timestampDelta){
        this.timestampDelta = timestampDelta;
    }

    public int getTimestamp(){
        return timestamp;
    }

    public int getTimestampDelta(){
        return timestampDelta;
    }
}
