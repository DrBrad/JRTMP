package unet.jrtmp.rtmp;

import unet.jrtmp.amf.AMF0;
import unet.jrtmp.handlers.RtmpHeader;
import unet.jrtmp.rtmp.messages.*;

import java.nio.ByteBuffer;

public class RtmpMessageDecoder {

    public static RtmpMessage decode(RtmpHeader header, ByteBuffer payload){
        RtmpMessage result;

        switch(header.getMessageTypeId()){
            case 1: { //SET CHUNK SIZE
                    int chunkSize = payload.getInt();
                    result = new SetChunkSize(chunkSize);
                }
                break;

            case 2: { //ABORT MESSAGE
                    int csid = payload.getInt();
                    result = new AbortMessage(csid);
                }
                break;

            case 3: { //ACKNOWLEDGEMENT
                    int sequenceNumber = payload.getInt();
                    result = new AcknowledgementMessage(sequenceNumber);
                }
                break;

            case 5: { //WINDOW ACKNOWLEDGEMENT
                    int windowSize = payload.getInt();
                    result = new WindowAcknowledgementSize(windowSize);
                }
                break;

            case 6: { //SET PEER BANDWIDTH
                    int ackSize = payload.getInt();
                    int type = payload.getInt();
                    result = new SetPeerBandwidth(ackSize, type);
                }
                break;

            case 20: { //COMMAND AMF0
                    result = new RtmpCommandMessage(new AMF0().decodeAll(payload));
                }
                break;

            case 4: { //USER CONTROL MESSAGE EVENTS
                    short eventType = payload.getShort();
                    int data = payload.getInt();
                    result = new UserControlMessageEvent(eventType, data);
                }
                break;

            case 8: { //TYPE AUDIO MESSAGE
                    AudioMessage am = new AudioMessage(payload.array());

                    //byte[] data = readAll(payload);
                    //am.setAudioData(data);

                    if(header.getFmt() == 0){
                        am.setTimestamp(header.getTimestamp());
                    }else if(header.getFmt() == 1 || header.getFmt() == 2){
                        am.setTimestampDelta(header.getTimestampDelta());
                    }
                    result = am;
                }
                break;

            case 9: { //TYPE VIDEO MESSAGE
                    VideoMessage vm = new VideoMessage(payload.array());

                    //byte[] data = readAll(payload);
                    //vm.setVideoData(payload);

                    if(header.getFmt() == 0){
                        vm.setTimestamp(header.getTimestamp());
                    }else if(header.getFmt() == 1 || header.getFmt() == 2){
                        vm.setTimestampDelta(header.getTimestampDelta());
                    }
                    result = vm;
                }
                break;

            case 18: { //TYPE DATA MESSAGE AMF0
                    result = new RtmpDataMessage(new AMF0().decodeAll(payload));
                }
                break;

            default:
                return null;
        }

        result.setInboundBodyLength(header.getMessageLength());
        result.setInboundHeaderLength(header.getHeaderLength());

        return result;
    }
}
