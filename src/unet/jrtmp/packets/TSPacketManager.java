package unet.jrtmp.packets;

import unet.jrtmp.rtmp.messages.AudioMessage;
import unet.jrtmp.rtmp.messages.RtmpMediaMessage;
import unet.jrtmp.rtmp.messages.VideoMessage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.zip.CRC32;

public class TSPacketManager extends PacketManager {


    public static final int TS_PACKET_SIZE = 188;

    private int pid = 0, continuity = 0, position = 0;

    //private TSPacket packet;
    private byte[] videoBuffer, audioBuffer;

    //private List<byte[]> segments;
    private OutputStream out;

    public TSPacketManager(){
        videoBuffer = new byte[TS_PACKET_SIZE];
        audioBuffer = new byte[TS_PACKET_SIZE];
        //segments = new ArrayList<>();
        try{
            out = new FileOutputStream(new File("/home/brad/Downloads/test.ts"));

            //WRITE PAT
            out.write(generatePATSegment());

            System.out.println();

            //WRITE PMT
            out.write(generatePMTSegment());

            System.out.println();

            //WRITE PCR
            out.write(generatePCRSegment());

            //THEN WRITE THE DATA PACKETS
            //4 byte header - 184 bytes of payload segments
            //ENSURE PID and CONTENUITY are correct for both
            //ENSURE that AUDIO buffer is not mixed with VIDEO buffer

            //WE NEED TO ENSURE WE ARE NOT MIXING AUDIO AND VIDEO WITHIN THE BUFFER....

            //writeTSData(0x0000, createPATPayload());
            //writeTSData(0x1000, createPMTPayload());


        }catch(IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void add(RtmpMediaMessage message){
        int dataLength = message.raw().length;

        //ENSURE PID...

        if(message instanceof AudioMessage){
            if(dataLength <= TS_PACKET_SIZE-4-position){
                // Append the entire data to the current segment
                System.arraycopy(message.raw(), 0, videoBuffer, position, dataLength);
                position += dataLength;

            }else{
                int offset = 0;

                while(offset < dataLength){
                    int bytesToCopy = Math.min(TS_PACKET_SIZE-4-position, dataLength-offset);
                    System.arraycopy(message.raw(), offset, videoBuffer, position, bytesToCopy);
                    offset += bytesToCopy;
                    position += bytesToCopy;

                    if(position == TS_PACKET_SIZE-4){
                        //segments.add(buffer);
                        write(new TSPacket(videoBuffer, pid, continuity));
                        continuity++;
                        videoBuffer = new byte[TS_PACKET_SIZE-4];
                        position = 0;
                    }
                }
            }

        }else if(message instanceof VideoMessage){
            if(dataLength <= TS_PACKET_SIZE-4-position){
                // Append the entire data to the current segment
                System.arraycopy(message.raw(), 0, audioBuffer, position, dataLength);
                position += dataLength;

            }else{
                int offset = 0;

                while(offset < dataLength){
                    int bytesToCopy = Math.min(TS_PACKET_SIZE-4-position, dataLength-offset);
                    System.arraycopy(message.raw(), offset, audioBuffer, position, bytesToCopy);
                    offset += bytesToCopy;
                    position += bytesToCopy;

                    if(position == TS_PACKET_SIZE-4){
                        //segments.add(buffer);
                        write(new TSPacket(audioBuffer, pid, continuity));
                        continuity++;
                        audioBuffer = new byte[TS_PACKET_SIZE-4];
                        position = 0;
                    }
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

    private byte[] generatePATSegment(){
        byte[] segment = new byte[TS_PACKET_SIZE];

        segment[0] = 0x47; // Sync byte
        segment[1] = 0x40; // Transport Error Indicator, Payload Unit Start Indicator, and Transport Priority
        segment[2] = 0x00; // PID (Program ID)
        segment[3] = 0x10; // Transport Scrambling Control, Adaptation Field Control, and Continuity Counter
        segment[4] = 0x00; // Payload (Program Association Table)

        // Set the PAT section length (including the CRC)
        segment[5] = 0x00; // High byte of section length (always zero for PAT)
        segment[6] = 0x0D; // Low byte of section length (13 bytes excluding the first 5 bytes)

        // Set the Program Number and PID for the first program
        segment[7] = 0x00; // Program Number (0)
        segment[8] = 0x00; // High byte of PID for Program Map Table (0x0001)
        segment[9] = 0x01; // Low byte of PID for Program Map Table (0x00C1)

        // Calculate and set the correct CRC32 value (based on the bytes)
        int crc32 = (int) calculateCRC32(segment, 5, 9);
        segment[10] = (byte) ((crc32 >> 24) & 0xFF);
        segment[11] = (byte) ((crc32 >> 16) & 0xFF);
        segment[12] = (byte) ((crc32 >> 8) & 0xFF);
        segment[13] = (byte) (crc32 & 0xFF);

        for(int i = 14; i < TS_PACKET_SIZE; i++){
            segment[i] = (byte) 0xFF;
        }

        for(int i = 0; i < segment.length; i++){
            System.out.printf("%02X ", segment[i]);
        }

        // Output the PAT segment in hexadecimal format
        /*
        for (int i = 0; i < 188; i++) {
            System.out.printf("%02X ", segment[i]);
        }

        /*
        // Set the Sync Byte (always 0x47)
        segment[0] = 0x47;

        // Set the Transport Error Indicator, Payload Unit Start Indicator, and Transport Priority
        // In this example, we're not setting any errors or priorities, so these bits are all set to 0
        segment[1] = 0x00;

        // Set the PID for the PAT (Program Association Table)
        // The PID for the PAT is always 0x0000
        segment[2] = 0x00;
        segment[3] = 0x00;

        // Set the Transport Scrambling Control to '00' (no scrambling)
        segment[4] = 0x00;

        // Set the Adaptation Field Control to '01' (indicating the presence of an adaptation field)
        // Note: In this example, we're not adding an adaptation field, so the remaining bits are set to '00'
        segment[5] = 0x10;

        // Set the Continuity Counter (increment this for each PAT packet)
        // In this example, we're starting with a counter value of 0
        segment[6] = 0x00;
        */

        return segment;
    }

    private long calculateCRC32(byte[] data, int start, int end){
        CRC32 crc32 = new CRC32();
        crc32.update(data, start, end - start + 1);
        return crc32.getValue();
    }

    private byte[] generatePMTSegment(){
        byte[] segment = new byte[TS_PACKET_SIZE];

        // Set the values for the PMT header
        segment[0] = 0x47; // Sync byte
        segment[1] = 0x40; // Transport Error Indicator, Payload Unit Start Indicator, and Transport Priority
        segment[2] = 0x00; // PID (Program ID)
        segment[3] = 0x10; // Transport Scrambling Control, Adaptation Field Control, and Continuity Counter
        segment[4] = 0x00; // Payload (Program Map Table)

        // Set the PMT section length (including the adaptation field if present)
        segment[5] = 0x00; // High byte of section length (always zero for PMT)
        segment[6] = 0x0D; // Low byte of section length (13 bytes excluding the first 5 bytes)

        for(int i = 14; i < TS_PACKET_SIZE; i++){
            segment[i] = (byte) 0xFF;
        }

        // Add your own data here (modify as needed)
        // Bytes 7 to 187 are available for your data

        // Output the PMT segment in hexadecimal format
        for(int i = 0; i < segment.length; i++){
            System.out.printf("%02X ", segment[i]);
        }

        return segment;
    }

    private byte[] generatePCRSegment(){
        byte[] segment = new byte[TS_PACKET_SIZE];

        // Set the values for the PCR header
        segment[0] = 0x47; // Sync byte
        segment[1] = (byte) 0x50; // Transport Error Indicator, Payload Unit Start Indicator, and Transport Priority
        segment[2] = 0x00; // PID (Program ID)
        segment[3] = 0x10; // Transport Scrambling Control, Adaptation Field Control, and Continuity Counter
        segment[4] = 0x00; // Payload (PCR)

        // Set the PCR section length (including the adaptation field if present)
        segment[5] = 0x00; // High byte of section length (always zero for PCR)
        segment[6] = (byte) 0xB0; // Low byte of section length (176 bytes excluding the first 5 bytes)

        // Add your own PCR data here (modify as needed)
        // Bytes 7 to 181 are available for your PCR data


        for(int i = 7; i < TS_PACKET_SIZE; i++){
            segment[i] = (byte) 0xFF;
        }

        // Output the PCR segment in hexadecimal format
        for(int i = 0; i < segment.length; i++){
            System.out.printf("%02X ", segment[i]);
        }

        return segment;
    }

    /*
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
