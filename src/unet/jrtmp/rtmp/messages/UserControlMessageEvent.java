package unet.jrtmp.rtmp.messages;

import unet.jrtmp.rtmp.RtmpMessage;

public class UserControlMessageEvent extends RtmpMessage {

    private short eventType;
    private int data;

    public UserControlMessageEvent(short eventType, int data){
        this.eventType = eventType;
        this.data = data;
    }

    @Override
    public int getOutboundCsid(){
        return 2;
    }

    @Override
    public int getMessageType(){
        return 4;
    }

    @Override
    public byte[] encodePayload(){
        return new byte[]{
                ((byte) (0xff & (eventType >> 8))),
                ((byte) (0xff & eventType)),
                ((byte) (0xff & (data >> 24))),
                ((byte) (0xff & (data >> 16))),
                ((byte) (0xff & (data >> 8))),
                ((byte) (0xff & data))
        };
    }

    public short getEventType(){
        return eventType;
    }

    public int getData(){
        return data;
    }

    /*
    public static UserControlMessageEvent streamBegin(int streamId) {
		UserControlMessageEvent e = new UserControlMessageEvent((short) 0,streamId );
		return e;
	}

	public static UserControlMessageEvent streamEOF(int streamId) {
		UserControlMessageEvent e = new UserControlMessageEvent((short) 1,streamId);
		return e;
	}

	public static UserControlMessageEvent streamDry(int streamId) {
		UserControlMessageEvent e = new UserControlMessageEvent((short) 2,streamId);
		return e;
	}

	public static UserControlMessageEvent setBufferLength(int bufferLengthInms) {
		UserControlMessageEvent e = new UserControlMessageEvent((short) 3,bufferLengthInms);
		return e;
	}

	public boolean isBufferLength() {
		return eventType==3;
	}
	*/
}
