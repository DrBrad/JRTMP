package unet.jrtmp.rtmp.messages;

public class WindowAcknowledgementSize extends RtmpControlMessage {

    private int windowSize;

    public WindowAcknowledgementSize(int windowSize){
        this.windowSize = windowSize;
    }

    @Override
    public int getMessageType(){
        return 5;
    }

    @Override
    public byte[] encodePayload(){
        return new byte[]{
                ((byte) (0xff & (windowSize >> 24))),
                ((byte) (0xff & (windowSize >> 16))),
                ((byte) (0xff & (windowSize >> 8))),
                ((byte) (0xff & windowSize))
        };
    }

    public int getWindowSize(){
        return windowSize;
    }
}
