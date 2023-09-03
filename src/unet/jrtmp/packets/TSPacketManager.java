package unet.jrtmp.packets;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static unet.jrtmp.packets.TSPacket.TS_PACKET_SIZE;

public class TSPacketManager extends PacketManager {

    private int pid = 0, continuity = 0, position = 0;

    //private TSPacket packet;
    private byte[] buffer;

    //private List<byte[]> segments;
    private OutputStream out;

    public TSPacketManager(){
        buffer = new byte[TS_PACKET_SIZE];
        //segments = new ArrayList<>();
        try{
            out = new FileOutputStream(new File("/home/brad/Downloads/test.ts"));
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void add(byte[] payload){
        int dataLength = payload.length;

        if(dataLength <= TS_PACKET_SIZE-position){
            // Append the entire data to the current segment
            System.arraycopy(payload, 0, buffer, position, dataLength);
            position += dataLength;

        }else{
            int offset = 0;

            while(offset < dataLength){
                int bytesToCopy = Math.min(TS_PACKET_SIZE-position, dataLength-offset);
                System.arraycopy(payload, offset, buffer, position, bytesToCopy);
                offset += bytesToCopy;
                position += bytesToCopy;

                if(position == TS_PACKET_SIZE){
                    //segments.add(buffer);
                    write(new TSPacket(buffer, pid, continuity));
                    continuity++;
                    buffer = new byte[TS_PACKET_SIZE];
                    position = 0;
                }
            }
        }
    }

    @Override
    public void write(Packet packet){
        //packet.getRaw());
        try{
            out.write(packet.getRaw());
            out.flush();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    /*
    public List<byte[]> getSegments(){
        return segments;
    }
    */
}
