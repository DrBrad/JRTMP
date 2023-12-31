package unet.jrtmp;

import unet.jrtmp.amf.AMF0Object;
import unet.jrtmp.handlers.ChunkDecoder;
import unet.jrtmp.handlers.ChunkEncoder;
import unet.jrtmp.rtmp.messages.RtmpMessage;
import unet.jrtmp.rtmp.messages.*;
import unet.jrtmp.stream.Stream;
import unet.jrtmp.stream.StreamManager;
import unet.jrtmp.stream.StreamName;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static unet.jrtmp.handlers.HandShakeDecoder.startHandshake;

public class RtmpSocket extends Thread {

    private Socket socket;
    private StreamManager streamManager;

    //IT WOULD PROBABLY BE BETTER TO JUST STORE THE STREAM HERE...


    private ChunkDecoder chunkDecoder;
    private ChunkEncoder chunkEncoder;

    private StreamName streamName;
    private int ackWindowSize, lastSentbackSize, bytesReceived;

    public RtmpSocket(Socket socket, StreamManager streamManager){
        this.socket = socket;
        this.streamManager = streamManager;
    }

    @Override
    public void run(){
        try{
            socket.setKeepAlive(true);

            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            /*
            * IT APPEARS THE WAY THIS WORKS IS
            * CONNECTION ADAPTER... ?
            * HANDSHAKE DECODER
            * CHUNK DECODE
            * CHUNK ENCODE
            * RTMP MESSAGE HANDLER
            * */

            /*
            byte[] packetData = new byte[]{
                    0x03, 0x00, 0x0B, 0x68, 0x00, 0x00, 0x19, 0x14, 0x00, 0x00, 0x00, 0x00,
                    0x02, 0x00, 0x0C, 0x63, 0x72, 0x65, 0x61, 0x74, 0x65, 0x53, 0x74, 0x72, 0x65, 0x61, 0x6D,
                    0x00, 0x40, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x05
            };

            ByteArrayInputStream in = new ByteArrayInputStream(packetData);
            */



            if(startHandshake(in, out)){
                //READ HEADER

                //LOOP
                // READ HEADER
                // READ PAYLOAD

                chunkDecoder = new ChunkDecoder(in);
                chunkEncoder = new ChunkEncoder(out);

                while(socket.isConnected()){
                    RtmpMessage message = chunkDecoder.decode();
                    maySendAck(message);

                    //System.out.println(message.getMessageType());

                    /*
                    if (!(msg instanceof VideoMessage || msg instanceof AudioMessage)) {
                        log.info("RTMP_Message_Read : {}", msg);
                    }
                    */

                    if(message instanceof WindowAcknowledgementSize){
                        ackWindowSize = ((WindowAcknowledgementSize) message).getWindowSize();
                        return;
                    }

                    if(message instanceof RtmpCommandMessage){
                        handleCommand((RtmpCommandMessage) message);

                    }else if(message instanceof RtmpDataMessage){
                        handleDataMessage((RtmpDataMessage) message);

                    }else if(message instanceof RtmpMediaMessage){
                        handleMedia((RtmpMediaMessage) message);

                    }else if(message instanceof UserControlMessageEvent){
                        handleUserControl((UserControlMessageEvent) message);
                    }
                }
            }

            socket.close();

        }catch(IOException e){
            e.printStackTrace();
        }
    }


    //NOT SURE HOW WE ARE DECODING MESSAGE...
    private void maySendAck(RtmpMessage message)throws IOException {
        int receiveBytes = message.getInboundBodyLength()+message.getInboundHeaderLength();
        bytesReceived += receiveBytes;

        if(ackWindowSize <= 0){
            return;
        }

        if(bytesReceived > 0X70000000){
            chunkEncoder.encode(new AcknowledgementMessage(bytesReceived));
            bytesReceived = 0;
            lastSentbackSize = 0;
            return;
        }

        if(bytesReceived - lastSentbackSize >= ackWindowSize){
            lastSentbackSize = bytesReceived;
            chunkEncoder.encode(new AcknowledgementMessage(lastSentbackSize));
        }
    }

    private void handleCommand(RtmpCommandMessage message)throws IOException {
        List<Object> command = message.getCommands();
        String commandName = (String) command.get(0);

        switch(commandName){
            case "connect":
                handleConnect(message);
                break;

            case "createStream":
                handleCreateStream(message);
                break;

            case "publish":
                handlePublish(message);
                break;

            case "play":
                handlePlay(message);
                break;

            case "deleteStream":
            case "closeStream":
                handleCloseStream(message);
                break;

            default:
                break;
        }
    }

    private void handleDataMessage(RtmpDataMessage message){
        String name = (String) message.getData().get(0);
        if(name.equals("@setDataFrame")){
            // save on metadata
            Map<String, Object> properties = (Map<String, Object>) message.getData().get(2);
            properties.remove("filesize");

            String encoder = (String) properties.get("encoder");
            if(encoder != null && encoder.contains("obs")){
                streamName.setObsClient(true);
            }

            Stream stream = streamManager.get(streamName);
            stream.setMetadata(properties);
        }
    }

    private void handleMedia(RtmpMediaMessage message){
        Stream stream = streamManager.get(streamName);

        if(stream == null){
            return;
        }

        stream.add(message);
    }

    private void handleUserControl(UserControlMessageEvent message){
//		boolean isBufferLength = msg.isBufferLength();
//		if (isBufferLength) {
//			if (role == Role.Subscriber) {
//				startPlay(ctx, streamManager.getStream(streamName));
//			}
//		}
    }

    private void handleConnect(RtmpCommandMessage message)throws IOException {
        // client send connect
        // server reply windows ack size and set peer bandwidth

        String app = (String) ((Map) message.getCommands().get(2)).get("app");
        Integer clientRequestEncode = (Integer) ((Map) message.getCommands().get(2)).get("objectEncoding");

        if(clientRequestEncode != null && clientRequestEncode.intValue() == 3){
            socket.close();
            return;
        }

        streamName = new StreamName(app);

        int ackSize = 5000000;
        WindowAcknowledgementSize was = new WindowAcknowledgementSize(ackSize);

        SetPeerBandwidth spb = new SetPeerBandwidth(ackSize, 2);

        SetChunkSize setChunkSize = new SetChunkSize(5000);

        chunkEncoder.encode(was);
        chunkEncoder.encode(spb);
        chunkEncoder.encode(setChunkSize);

        List<Object> result = new ArrayList<>();
        result.add("_result");
        result.add(message.getCommands().get(1));// transaction id
        result.add(new AMF0Object()
                .addProperty("fmsVer", "FMS/3,0,1,123")
                .addProperty("capabilities", 31));

        result.add(new AMF0Object()
                .addProperty("level", "status")
                .addProperty("code", "NetConnection.Connect.Success")
                .addProperty("description", "Connection succeeded")
                .addProperty("objectEncoding", 0));

        RtmpCommandMessage response = new RtmpCommandMessage(result);

        chunkEncoder.encode(response);
    }

    private void handleCreateStream(RtmpCommandMessage message)throws IOException {
        List<Object> result = new ArrayList<>();
        result.add("_result");
        result.add(message.getCommands().get(1));// transaction id
        result.add(null);// properties
        result.add(5);// stream id

        RtmpCommandMessage response = new RtmpCommandMessage(result);
        chunkEncoder.encode(response);
    }

    private void handlePublish(RtmpCommandMessage message)throws IOException {
        //role = Role.Publisher;

        String streamType = (String) message.getCommands().get(4);
        if(!streamType.equals("live")){
            socket.close();
        }

        String name = (String) message.getCommands().get(3);
        streamName.setName(name);
        streamName.setApp(streamType);

        Stream stream = new Stream(streamName);
        //stream.setPublisher(ctx.channel());
        streamManager.add(streamName, stream);

        // reply a onStatus
        RtmpCommandMessage onStatus = onStatus("status", "NetStream.Publish.Start", "Start publishing");
        chunkEncoder.encode(onStatus);
    }

    private void handlePlay(RtmpCommandMessage message)throws IOException {
        //role = Role.Subscriber;

        String name = (String) message.getCommands().get(3);
        streamName.setName(name);

        Stream stream = streamManager.get(streamName);
        if(stream == null){
            // NetStream.Play.StreamNotFound
            RtmpCommandMessage onStatus = onStatus("error", "NetStream.Play.StreamNotFound", "No Such Stream");
            chunkEncoder.encode(onStatus);

            //normalShutdown = true;
            socket.close();

        }//else{
            //startPlay(ctx, stream);
        //}
    }

    private void handleCloseStream(RtmpCommandMessage message)throws IOException {
        //if(role == Role.Subscriber){
        //    return;
        //}

        // send back 'NetStream.Unpublish.Success' to publisher
        RtmpCommandMessage onStatus = onStatus("status", "NetStream.Unpublish.Success", "Stop publishing");
        chunkEncoder.encode(onStatus);
        // send User Control Message Stream EOF (1) to all subscriber
        // and we close all publisher and subscribers
        Stream stream = streamManager.get(streamName);

        if(stream != null){
            //stream.sendEofToAllSubscriberAndClose();
            streamManager.remove(streamName);
            //normalShutdown = true;
            socket.close();
        }
    }

    private RtmpCommandMessage onStatus(String level, String code, String description){
        List<Object> result = new ArrayList<>();
        result.add("onStatus");
        result.add(0);// always 0
        result.add(null);// properties
        result.add(new AMF0Object()
                .addProperty("level", level)
                .addProperty("code", code)
                .addProperty("description", description));// stream id

        RtmpCommandMessage response = new RtmpCommandMessage(result);
        return response;
    }
}
