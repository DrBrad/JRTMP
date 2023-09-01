package unet.jrtmp.rtmp.messages;

public class SetChunkSize extends RtmpControlMessage {

    private int chunkSize;

    public SetChunkSize(int chunkSize){
        this.chunkSize = chunkSize;
    }

    @Override
    public int getMessageType(){
        return 1;
    }

    @Override
    public byte[] encodePayload(){
        return new byte[]{
                ((byte) (0xff & (chunkSize >> 24))),
                ((byte) (0xff & (chunkSize >> 16))),
                ((byte) (0xff & (chunkSize >> 8))),
                ((byte) (0xff & chunkSize))
        };
    }

    public int getChunkSize(){
        return chunkSize;
    }
}
