package unet.jrtmp.rtmp.messages;

import unet.jrtmp.rtmp.RtmpMessage;

public abstract class RtmpMediaMessage extends RtmpMessage {

    private int timestamp, timestampDelta;

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
