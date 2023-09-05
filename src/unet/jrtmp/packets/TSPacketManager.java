package unet.jrtmp.packets;

import unet.jrtmp.rtmp.messages.AudioMessage;
import unet.jrtmp.rtmp.messages.RtmpMediaMessage;
import unet.jrtmp.rtmp.messages.VideoMessage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

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

            //WE NEED TO ENSURE WE ARE NOT MIXING AUDIO AND VIDEO WITHIN THE BUFFER....

            //writeTSData(0x0000, createPATPayload());
            //writeTSData(0x1000, createPMTPayload());


        }catch(IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void add(RtmpMediaMessage message){

        //WE DONT COMBINE.... WHAT A WASTE OF FUCKING TIME...

        try{
            out.write(message.raw());
            out.flush();
        }catch(IOException e){
            e.printStackTrace();
        }


        /*
        try{
            if(message instanceof AudioMessage){
                writeTSData(0x0110, message.raw());

            }else if(message instanceof VideoMessage){
                writeTSData(0x0100, message.raw());
            }
        }catch(IOException e){
            e.printStackTrace();
        }

        /*
        int dataLength = message.raw().length;

        if(dataLength <= TS_PACKET_SIZE-position){
            // Append the entire data to the current segment
            System.arraycopy(message.raw(), 0, buffer, position, dataLength);
            position += dataLength;

        }else{
            int offset = 0;

            while(offset < dataLength){
                int bytesToCopy = Math.min(TS_PACKET_SIZE-position, dataLength-offset);
                System.arraycopy(message.raw(), offset, buffer, position, bytesToCopy);
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
        */
    }

    @Override
    public void write(Packet packet){
        //packet.getRaw());
        /*
        try{
            out.write(packet.getRaw());
            out.flush();
        }catch(IOException e){
            e.printStackTrace();
        }
        */
    }




    private void writeTSData(int pid, byte[] data) throws IOException {
        // Split data into TS packets and write them to the output stream
        int dataLength = data.length;
        int packetCount = (int) Math.ceil((double) dataLength / (TS_PACKET_SIZE - 4));

        for (int i = 0; i < packetCount; i++) {
            int packetStart = i * (TS_PACKET_SIZE - 4);
            int packetEnd = Math.min(packetStart + (TS_PACKET_SIZE - 4), dataLength);

            // Create the TS packet
            byte[] tsPacket = new byte[TS_PACKET_SIZE];
            tsPacket[0] = 0x47;  // Sync byte
            tsPacket[1] = (byte) ((pid >> 8) & 0xFF);  // PID high byte
            tsPacket[2] = (byte) (pid & 0xFF);         // PID low byte

            // Adaptation field control, set to 0x01 for no adaptation field
            tsPacket[3] = (byte) 0x01;

            // Copy payload data into the packet
            System.arraycopy(data, packetStart, tsPacket, 4, packetEnd - packetStart);

            // Write the TS packet to the output stream
            out.write(tsPacket);
        }
    }


    private byte[] createPATPayload(){
        // Create a simple PAT payload
        byte[] patPayload = {
                0x00, 0x00,  // Table ID (PAT)
                0x00, (byte) 0xB0,  // Section syntax indicator, section length (11 bytes)
                0x00, 0x01,  // Transport stream ID
                0x00, 0x00,  // Version number and current next indicator
                0x00, 0x00,  // Section number and last section number
                0x00, 0x01   // Program number (0x0001) and PID (0x0100)
        };

        return patPayload;
    }

    private byte[] createPMTPayload(){
        // Create a simple PMT payload (for video and audio streams)
        byte[] pmtPayload = {
                0x02, (byte) 0xB0,  // Table ID (PMT), section syntax indicator, section length (11 bytes)
                0x00, 0x01,  // Program number (0x0001)
                0x00, 0x00,  // Version number and current next indicator
                0x00, 0x00,  // Section number and last section number
                0x1B, (byte) 0xE0,  // PCR PID (0x1BE0)
                0x00, (byte) 0xF0,  // Program info length (0x00F0)

                // Video Stream Info
                0x1B, (byte) 0xE0,  // Stream type (H.264 video), elementary PID (0x1BE0)
                0x00, 0x00,  // Reserved bits
                0x00, (byte) 0xF0,  // ES Info length (0x00F0)

                // Audio Stream Info (AAC audio)
                0x0F, (byte) 0xE1,  // Stream type (AAC audio), elementary PID (0x0FE1)
                0x00, 0x00,  // Reserved bits
                0x00, 0x00   // ES Info length (0x0000)
        };

        return pmtPayload;
    }

    /*
    public List<byte[]> getSegments(){
        return segments;
    }
    */
}
