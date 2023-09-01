package unet.jrtmp.rtmp;

import unet.jrtmp.amf.AMF0;
import unet.jrtmp.handlers.RtmpHeader;
import unet.jrtmp.rtmp.messages.*;

import static unet.jrtmp.handlers.ByteUtils.readInt;
import static unet.jrtmp.handlers.ByteUtils.readShort;

public class RtmpMessageDecoder {

    public RtmpMessageDecoder(){
    }

    public static RtmpMessage decode(RtmpHeader header, byte[] payload){
        RtmpMessage result;

        switch(header.getMessageTypeId()){
            case 1: { //SET CHUNK SIZE
                    int chunkSize = readInt(payload, 0);
                    result = new SetChunkSize(chunkSize);
                }
                break;

            case 2: { //ABORT MESSAGE
                    int csid = readInt(payload, 0);
                    result = new AbortMessage(csid);
                }
                break;

            case 3: { //ACKNOWLEDGEMENT
                    int sequenceNumber = readInt(payload, 0);
                    result = new AcknowledgementMessage(sequenceNumber);
                }
                break;

            case 5: { //WINDOW ACKNOWLEDGEMENT
                    int windowSize = readInt(payload, 0);
                    result = new WindowAcknowledgementSize(windowSize);
                }
                break;

            case 6: { //SET PEER BANDWIDTH
                    int ackSize = readInt(payload, 0);
                    int type = readInt(payload, 4);
                    result = new SetPeerBandwidth(ackSize, type);
                }
                break;

            case 20: { //COMMAND AMF0
                    result = new RtmpCommandMessage(new AMF0(payload).decodeAll());
                }
                break;

            case 4: { //USER CONTROL MESSAGE EVENTS
                    short eventType = readShort(payload, 0);
                    int data = readInt(payload, 2);
                    result = new UserControlMessageEvent(eventType, data);
                }
                break;

            case 8: { //TYPE AUDIO MESSAGE
                    AudioMessage am = new AudioMessage(payload);

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
                    VideoMessage vm = new VideoMessage(payload);

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
                    result = new RtmpDataMessage(new AMF0(payload).decodeAll());
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
