package unet.jrtmp.packets.mpegts;

import unet.jrtmp.packets.Packet;
import unet.jrtmp.packets.PacketManager;
import unet.jrtmp.rtmp.messages.AudioMessage;
import unet.jrtmp.rtmp.messages.RtmpMediaMessage;
import unet.jrtmp.rtmp.messages.VideoMessage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

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


            /*
            for(int i = 0; i < segment.length; i++){
                System.out.printf("%02X ", segment[i]);
            }
            */

             byte[] b = new byte[]{
                    /* TS */
                    0x47, 0x40, 0x00, 0x10, 0x00,
                    /* PSI */
                    0x00, (byte) 0xb0, 0x0d, 0x00, 0x01, (byte) 0xc1, 0x00, 0x00,
                    /* PAT */
                    0x00, 0x01, (byte) 0xf0, 0x01,
                    /* CRC */
                    0x2e, 0x70, 0x19, 0x05,
                    /* stuffing 167 bytes */
                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,

                    /* TS */
                    0x47, 0x50, 0x01, 0x10, 0x00,
                    /* PSI */
                    0x02, (byte) 0xb0, 0x17, 0x00, 0x01, (byte) 0xc1, 0x00, 0x00,
                    /* PMT */
                     (byte) 0xe1, 0x00,
                     (byte) 0xf0, 0x00,
                    0x1b, (byte) 0xe1, 0x00, (byte) 0xf0, 0x00, /* h264 */
                    0x0f, (byte) 0xe1, 0x01, (byte) 0xf0, 0x00, /* aac */
                    /*0x03, 0xe1, 0x01, 0xf0, 0x00,*/ /* mp3 */
                    /* CRC */
                    0x2f, 0x44, (byte) 0xb9, (byte) 0x9b, /* crc for aac */
                    /*0x4e, 0x59, 0x3d, 0x1e,*/ /* crc for mp3 */
                    /* stuffing 157 bytes */
                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff
            };

             out.write(b);

             /*
            //WRITE PAT
            TSPacket packet = new TSPATPacket(0, 0);
            out.write(packet.getEncoded());

            byte[] b = packet.getEncoded();
            for(int i = 0; i < b.length; i++){
                System.out.printf("%02X ", b[i]);
            }

            System.out.println();
            */

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
                        TSPacket packet = new TSMediaPacket(videoBuffer, 0, continuity);
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
                        TSPacket packet = new TSMediaPacket(audioBuffer, 1, continuity);
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
