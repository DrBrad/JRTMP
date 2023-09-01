package unet.jrtmp.handlers;

public class RtmpHeader {

    private int csid, fmt, timestamp, messageLength, messageStreamId, timestampDelta, headerLength;
    short messageTypeId;
    long extendedTimestamp;

    public boolean mayHaveExtendedTimestamp(){
        return (fmt==0 && timestamp ==  0xFFFFFF) || ( (fmt==1 || fmt==2) && timestampDelta ==  0xFFFFFF);
    }

    public void setCsid(int csid){
        this.csid = csid;
    }

    public int getCsid(){
        return csid;
    }

    public void setFmt(int fmt){
        this.fmt = fmt;
    }

    public int getFmt(){
        return fmt;
    }

    public void setTimestamp(int timestamp){
        this.timestamp = timestamp;
    }

    public int getTimestamp(){
        return timestamp;
    }

    public void setMessageLength(int messageLength){
        this.messageLength = messageLength;
    }

    public int getMessageLength(){
        return messageLength;
    }

    public void setMessageStreamId(int messageStreamId){
        this.messageStreamId = messageStreamId;
    }

    public int getMessageStreamId(){
        return messageStreamId;
    }

    public void setTimestampDelta(int timestampDelta){
        this.timestampDelta = timestampDelta;
    }

    public int getTimestampDelta(){
        return timestampDelta;
    }

    public void setHeaderLength(int headerLength){
        this.headerLength = headerLength;
    }

    public int getHeaderLength(){
        return headerLength;
    }

    public void setMessageTypeId(short messageTypeId){
        this.messageTypeId = messageTypeId;
    }

    public short getMessageTypeId(){
        return messageTypeId;
    }

    public void setExtendedTimestamp(long extendedTimestamp){
        this.extendedTimestamp = extendedTimestamp;
    }

    public long getExtendedTimestamp(){
        return extendedTimestamp;
    }

    @Override
    public String toString(){
        return "{\r\n" +
                "    CSID: "+csid+"\r\n" +
                "    FMT: "+fmt+"\r\n" +
                "    TIME-STAMP: "+timestamp+"\r\n" +
                "    MESSAGE-LENGTH: "+messageLength+"\r\n" +
                "    MESSAGE-STREAM-ID: "+messageStreamId+"\r\n" +
                "    TIME-STAMP-DELTA: "+timestampDelta+"\r\n" +
                "    HEADER-LENGTH: "+headerLength+"\r\n" +
                "    MESSAGE-TYPE-ID: "+messageTypeId+"\r\n" +
                "    EXTENDED-TIME-STAMP: "+extendedTimestamp+"\r\n" +
                "}";
    }
}
