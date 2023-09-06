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

    private int continuity = 0, position = 0;

    //private TSPacket packet;
    private byte[] videoBuffer, audioBuffer;

    //private List<byte[]> segments;
    private OutputStream out;

    public TSPacketManager(){
        videoBuffer = new byte[TS_PACKET_SIZE-4];
        audioBuffer = new byte[TS_PACKET_SIZE-4];
        //segments = new ArrayList<>();
        try{
            out = new FileOutputStream(new File("/home/brad/Downloads/test.ts"));

            //WRITE PAT
            out.write(generatePATSegment());

            System.out.println();

            //WRITE PMT
            //out.write(generatePMTSegment());

            //System.out.println();

            //WRITE PCR
            //out.write(generatePCRSegment());

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

        if(message instanceof VideoMessage){
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
                        TSPacket packet = new TSPacket(videoBuffer);
                        packet.setPID(0);
                        packet.setContinuityCounter(continuity);
                        write(packet);
                        continuity = (continuity + 1) % 16;
                        videoBuffer = new byte[TS_PACKET_SIZE-4];
                        position = 0;
                    }
                }
            }

        }else if(message instanceof AudioMessage){
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
                        TSPacket packet = new TSPacket(audioBuffer);
                        packet.setPID(1);
                        packet.setContinuityCounter(continuity);
                        write(packet);
                        continuity = (continuity + 1) % 16;
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
            out.write(packet.getEncoded());
            out.flush();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private byte[] generatePATSegment(){
        byte[] segment = new byte[TS_PACKET_SIZE];


        //TS HEADER

        // Set the sync byte (always 0x47)
        segment[0] = 0x47;

        // Set the transport error indicator, payload unit start indicator, and transport priority
        segment[1] = (byte) 0x40;

        // Set the PID (Program ID) for the PAT (always 0x00 for PAT)
        segment[2] = 0x00;
        segment[3] = 0x10;

        //PADDING
        segment[4] = 0x00;
        segment[5] = 0x00;

        //PAT HEADER
        segment[6] = (byte) 0xB0; // PAT version number
        segment[7] = 0x0D; // PAT section number

        segment[8] = 0x00; // PAT program number
        segment[9] = 0x01; // Reserved

        segment[10] = 0x00; // Reserved
        segment[11] = 0x00; // Reserved


        //PROGRAM
        // Add the PID for the audio stream.
        segment[12] = (byte) 0x00;
        segment[13] = (byte) 0x01;

        // Add the PID for the video stream.
        segment[14] = (byte) 0x01;
        segment[15] = (byte) 0x00;


        int crc32 = (int) calculateCRC32(segment, 5, 9);
        segment[16] = (byte) ((crc32 >> 24) & 0xFF);
        segment[17] = (byte) ((crc32 >> 16) & 0xFF);
        segment[18] = (byte) ((crc32 >> 8) & 0xFF);
        segment[19] = (byte) (crc32 & 0xFF);



        // Set the transport scrambling control, adaptation field control, and continuity counter


        /*
        segment[4] = (byte) 0x10;

        // Set the PAT section length (always 0x00 for PAT)
        segment[5] = 0x00;
        segment[6] = 0x00;

        // Set the program number and PID for the first program
        segment[7] = 0x00; // Program number (always 0x00 for PAT)
        segment[8] = 0x01; // PID high byte (0x0001)
        segment[9] = (byte) 0xC1; // PID low byte (0x00C1)
        */

        /*
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
        */

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

        segment[0] = 0x47; // Sync byte
        segment[1] = (byte) 0x40; // Transport Error Indicator, Payload Unit Start Indicator, and Transport Priority
        segment[2] = 0x00; // PID (Program ID) - High byte
        segment[3] = (byte) 0x10; // PID (Program ID) - Low byte

        segment[0] = 0x00; // PAT version number
        segment[1] = 0x00; // PAT section number
        segment[2] = 0x00; // PAT program number

        segment[3] = 0x01; // Reserved
        segment[4] = 0x00; // Reserved
        segment[5] = 0x00; // Reserved

        // Add the PID for the audio stream.
        segment[6] = (byte) 0x00;
        segment[7] = (byte) 0x01;

        // Add the PID for the video stream.
        segment[8] = (byte) 0x01;
        segment[9] = (byte) 0x00;

        /*
        segment[4] = 0x00; // Transport Scrambling Control, Adaptation Field Control, and Continuity Counter
        segment[5] = (byte) 0xB0; // Payload (Program Map Table)
        segment[6] = 0x0D; // Payload (Program Map Table) - Length
        segment[7] = 0x00; // Program Number - High byte
        segment[8] = 0x01; // Program Number - Low byte
        segment[9] = (byte) 0xC1; // Program Map PID - High byte
        segment[10] = 0x00; // Program Map PID - Low byte
        segment[11] = 0x00; // Unused
        segment[12] = 0x01; // CRC32 - Byte 1
        segment[13] = (byte) 0xF0; // CRC32 - Byte 2
        segment[14] = 0x00; // CRC32 - Byte 3
        segment[15] = 0x2A; // CRC32 - Byte 4
        /*
        // Set the values for the PMT header
        segment[0] = 0x47; // Sync byte
        segment[1] = 0x40; // Transport Error Indicator, Payload Unit Start Indicator, and Transport Priority
        segment[2] = 0x00; // PID (Program ID)
        segment[3] = 0x10; // Transport Scrambling Control, Adaptation Field Control, and Continuity Counter
        segment[4] = 0x00; // Payload (Program Map Table)

        // Set the PMT section length (including the adaptation field if present)
        segment[5] = 0x00; // High byte of section length (always zero for PMT)
        segment[6] = 0x0D; // Low byte of section length (13 bytes excluding the first 5 bytes)
        */

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
}
