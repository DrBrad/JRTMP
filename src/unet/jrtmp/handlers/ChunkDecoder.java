package unet.jrtmp.handlers;

import unet.jrtmp.rtmp.RtmpMessage;
import unet.jrtmp.rtmp.RtmpMessageDecoder;
import unet.jrtmp.rtmp.messages.SetChunkSize;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;

public class ChunkDecoder {

    private int clientChunkSize = 128, currentCsid;//, payloadPosition;
    private HashMap<Integer/* csid */, RtmpHeader> prevousHeaders;
    private HashMap<Integer/* csid */, ByteBuffer> inCompletePayload;

    private ByteBuffer currentPayload;

    private InputStream in;
    //private OutputStream out;

    public ChunkDecoder(InputStream in/*, OutputStream out*/){
        this.in = in;
        //this.out = out;

        prevousHeaders = new HashMap<>(4);
        inCompletePayload = new HashMap<>(4);
    }

    public void decode()throws IOException {
        RtmpHeader header = readHeader();
        completeHeader(header);

        currentCsid = header.getCsid();

        if(header.getFmt() != 3){
            ByteBuffer buffer = ByteBuffer.allocate(header.getMessageLength());
            //byte[] buffer = new byte[header.getMessageLength()];
            inCompletePayload.put(header.getCsid(), buffer);
            prevousHeaders.put(header.getCsid(), header);
        }

        currentPayload = inCompletePayload.get(header.getCsid());
        if(currentPayload == null){
            RtmpHeader previousHeader = prevousHeaders.get(header.getCsid());
            currentPayload = ByteBuffer.allocate(previousHeader.getMessageLength());//new byte[previousHeader.getMessageLength()];
            inCompletePayload.put(header.getCsid(), currentPayload);
        }

        System.out.println(header);

        //System.out.println(in.available()+"  "+payloadPosition);

        byte[] bytes = new byte[Math.min(currentPayload.remaining(), clientChunkSize)];
        in.read(bytes);
        currentPayload.put(bytes);
        //int length = in.read(currentPayload, 0, Math.min(currentPayload.remaining(), clientChunkSize));
        //payloadPosition += length; //WE NEED TO FIGURE OUT WHAT TO DO WITH THIS...

        if(currentPayload.hasRemaining()){
            System.out.println("LENGTH ISSUE");
            decode();
            return;
        }

        inCompletePayload.remove(currentCsid);

        currentPayload.flip();
        RtmpMessage message = RtmpMessageDecoder.decode(prevousHeaders.get(currentCsid), currentPayload);

        //if(message instanceof )

        if(message instanceof SetChunkSize){
            SetChunkSize scs = (SetChunkSize) message;
            clientChunkSize = scs.getChunkSize();
            //log.debug("------------>client set chunkSize to :{}", clientChunkSize);
        }else{
            //out.add(msg);
        }

    }

    /*
    public void decodePayload()throws IOException {
        int length = in.read(currentPayload, 0, Math.min(currentPayload.length, clientChunkSize));

        if(length < currentPayload.length){
            return;
        }

        inCompletePayload.remove(currentCsid);

        RtmpHeader header = prevousHeaders.get(currentCsid);
        RtmpMessage msg = RtmpMessageDecoder.decode(header, currentPayload);

        /*
        if (msg instanceof SetChunkSize) {
            // We need chunksize to decode the chunk
            SetChunkSize scs = (SetChunkSize) msg;
            clientChunkSize = scs.getChunkSize();
            log.debug("------------>client set chunkSize to :{}", clientChunkSize);
        } else {
            out.add(msg);
        }
        *./
    }

    //public void decode(InputStream in)throws IOException {
        //NOT SURE ABOUT THIS STATE SHIT...

        //currentPayload.writeBytes(bytes);
        //checkpoint(DecodeState.STATE_HEADER);

        /*
        if(currentPayload.isWritable()){
            return;
        }
        inCompletePayload.remove(currentCsid);

        // then we can decode out payload
        ByteBuf payload = currentPayload;
        RtmpHeader header = prevousHeaders.get(currentCsid);

        RtmpMessage msg = RtmpMessageDecoder.decode(header, payload);
        if (msg == null) {
            log.error("RtmpMessageDecoder.decode NULL");
            return;
        }

        if (msg instanceof SetChunkSize) {
            // we need chunksize to decode the chunk
            SetChunkSize scs = (SetChunkSize) msg;
            clientChunkSize = scs.getChunkSize();
            log.debug("------------>client set chunkSize to :{}", clientChunkSize);
        } else {
            out.add(msg);
        }
        */

        /*
        DecodeState state = DecodeState.STATE_HEADER;

        //IF STATE HEADER...
        switch(state){
            case STATE_HEADER:
                RtmpHeader header = readHeader(in);
                completeHeader(header);
                currentCsid = header.getCsid();

                if(header.getFmt() != 3){
                    byte[] buffer = new byte[header.getMessageLength()];
                    inCompletePayload.put(header.getCsid(), buffer);
                    prevousHeaders.put(header.getCsid(), header);
                }

                currentPayload = inCompletePayload.get(header.getCsid());
                if(currentPayload == null){
                    RtmpHeader previousHeader = prevousHeaders.get(header.getCsid());
                    currentPayload = new byte[previousHeader.getMessageLength()];
                    inCompletePayload.put(header.getCsid(), currentPayload);
                }

                System.out.println(header);

                break;

            case STATE_PAYLOAD:
                /*
                final byte[] bytes = new byte[Math.min(currentPayload.writableBytes(), clientChunkSize)];
                in.read(bytes);
                currentPayload.writeBytes(bytes);
                checkpoint(DecodeState.STATE_HEADER);

                if(currentPayload.isWritable()){
                    return;
                }
                inCompletePayload.remove(currentCsid);

                // then we can decode out payload
                ByteBuf payload = currentPayload;
                RtmpHeader header = prevousHeaders.get(currentCsid);

                RtmpMessage msg = RtmpMessageDecoder.decode(header, payload);
                if (msg == null) {
                    log.error("RtmpMessageDecoder.decode NULL");
                    return;
                }

                if (msg instanceof SetChunkSize) {
                    // we need chunksize to decode the chunk
                    SetChunkSize scs = (SetChunkSize) msg;
                    clientChunkSize = scs.getChunkSize();
                    log.debug("------------>client set chunkSize to :{}", clientChunkSize);
                } else {
                    out.add(msg);
                }
                *./
                break;

            default:
                break;
        }*/



        //IF STATE PAYLOAD
    //}

    private RtmpHeader readHeader()throws IOException {
        RtmpHeader header = new RtmpHeader();

        int headerLength = 0;

        byte firstByte = (byte) in.read();
        headerLength += 1;

        // CHUNK HEADER is divided into
        // BASIC HEADER
        // MESSAGE HEADER
        // EXTENDED TIMESTAMP

        // BASIC HEADER
        // fmt and chunk steam id in first byte
        int fmt = (firstByte & 0xff) >> 6;
        int csid = (firstByte & 0x3f);

        if(csid == 0){
            // 2 byte form
            csid = (byte) in.read() & 0xff + 64;
            headerLength += 1;

        }else if(csid == 1){
            // 3 byte form
            byte secondByte = (byte) in.read();
            byte thirdByte = (byte) in.read();
            csid = (thirdByte & 0xff) << 8 + (secondByte & 0xff) + 64;
            headerLength += 2;

        }else if(csid >= 2){
            // that's it!
        }

        header.setCsid(csid);
        header.setFmt(fmt);

        switch(fmt){
            case 0: {
                    int timestamp = ((in.read() << 16)
                            | (in.read() << 8)
                            | in.read());
                    int messageLength = ((in.read() << 16)
                            | (in.read() << 8)
                            | in.read());
                    short messageTypeId = (short) (in.read() & 0xff);
                    int messageStreamId = (((in.read() & 0xff) << 24)
                            | ((in.read() & 0xff) << 16)
                            | ((in.read() & 0xff) << 8)
                            | (in.read() & 0xff));
                    headerLength += 11;
                    if(timestamp == 0XFFFFFF){
                        long extendedTimestamp = (((in.read() & 0xff) << 24)
                                | ((in.read() & 0xff) << 16)
                                | ((in.read() & 0xff) << 8)
                                | (in.read() & 0xff));
                        header.setExtendedTimestamp(extendedTimestamp);
                        headerLength += 4;
                    }

                    header.setTimestamp(timestamp);
                    header.setMessageTypeId(messageTypeId);
                    header.setMessageStreamId(messageStreamId);
                    header.setMessageLength(messageLength);
                }
                break;

            case 1: {
                    int timestampDelta = ((in.read() << 16)
                            | (in.read() << 8)
                            | in.read());
                    int messageLength = ((in.read() << 16)
                            | (in.read() << 8)
                            | in.read());
                    short messageType = (short) (in.read() & 0xff);

                    headerLength += 7;
                    if(timestampDelta == 0XFFFFFF){
                        long extendedTimestamp = (((in.read() & 0xff) << 24)
                                | ((in.read() & 0xff) << 16)
                                | ((in.read() & 0xff) << 8)
                                | (in.read() & 0xff));
                        header.setExtendedTimestamp(extendedTimestamp);
                        headerLength += 4;
                    }

                    header.setTimestampDelta(timestampDelta);
                    header.setMessageLength(messageLength);
                    header.setMessageTypeId(messageType);
                }
                break;

            case 2: {
                    int timestampDelta = ((in.read() << 16)
                            | (in.read() << 8)
                            | in.read());
                    headerLength += 3;
                    header.setTimestampDelta(timestampDelta);

                    if(timestampDelta == 0XFFFFFF){
                        long extendedTimestamp = (((in.read() & 0xff) << 24)
                                | ((in.read() & 0xff) << 16)
                                | ((in.read() & 0xff) << 8)
                                | (in.read() & 0xff));
                        header.setExtendedTimestamp(extendedTimestamp);
                        headerLength += 4;
                    }
                }
                break;

            case 3:
                // nothing
                break;

            default:
                throw new RuntimeException("illegal fmt type:" + fmt);

        }

        header.setHeaderLength(headerLength);

        return header;
    }

    private void completeHeader(RtmpHeader header){
        RtmpHeader prev = prevousHeaders.get(header.getCsid());
        if(prev == null){
            return;
        }

        switch(header.getFmt()){
            case 1:
                header.setMessageStreamId(prev.getMessageStreamId());
                break;

            case 2:
                header.setMessageLength(prev.getMessageLength());
                header.setMessageStreamId(prev.getMessageStreamId());
                header.setMessageTypeId(prev.getMessageTypeId());
                break;

            case 3:
                header.setMessageStreamId(prev.getMessageStreamId());
                header.setMessageTypeId(prev.getMessageTypeId());
                header.setTimestamp(prev.getTimestamp());
                header.setTimestampDelta(prev.getTimestampDelta());
                break;

            default:
                break;
        }
    }
}