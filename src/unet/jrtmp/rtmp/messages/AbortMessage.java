package unet.jrtmp.rtmp.messages;

public class AbortMessage extends RtmpControlMessage {

    private int csid;

    public AbortMessage(int csid){
        this.csid = csid;
    }

    @Override
    public byte[] encodePayload(){
        return new byte[]{
                ((byte) (0xff & (csid >> 24))),
                ((byte) (0xff & (csid >> 16))),
                ((byte) (0xff & (csid >> 8))),
                ((byte) (0xff & csid))
        };
    }

    @Override
    public int getMessageType(){
        return 2;
    }

    public int getCsid(){
        return csid;
    }
}
