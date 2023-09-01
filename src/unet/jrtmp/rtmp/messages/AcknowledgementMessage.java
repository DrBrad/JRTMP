package unet.jrtmp.rtmp.messages;

public class AcknowledgementMessage extends RtmpControlMessage {

    private int sequenceNumber;

    public AcknowledgementMessage(int sequenceNumber){
        this.sequenceNumber = sequenceNumber;
    }

    @Override
    public byte[] encodePayload(){
        return new byte[]{
                ((byte) (0xff & (sequenceNumber >> 24))),
                ((byte) (0xff & (sequenceNumber >> 16))),
                ((byte) (0xff & (sequenceNumber >> 8))),
                ((byte) (0xff & sequenceNumber))
        };
    }

    @Override
    public int getMessageType(){
        return 3;
    }

    public int getSequenceNumber(){
        return sequenceNumber;
    }
}
