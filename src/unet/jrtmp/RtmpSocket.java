package unet.jrtmp;

import unet.jrtmp.handlers.ChunkDecoder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import static unet.jrtmp.handlers.HandShakeDecoder.startHandshake;

public class RtmpSocket extends Thread {

    private Socket socket;

    private InputStream in;
    private OutputStream out;

    public RtmpSocket(Socket socket){
        this.socket = socket;
    }

    @Override
    public void run(){
        try{
            socket.setKeepAlive(true);

            in = socket.getInputStream();
            out = socket.getOutputStream();

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

                ChunkDecoder chunkDecoder = new ChunkDecoder(in);
                chunkDecoder.decode();

                //ChunkEncoder chunkEncoder = new ChunkEncoder(out);
                //chunkEncoder.encode();
            }

            socket.close();

        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
