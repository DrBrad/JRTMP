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
                        TSPacket packet = new TSPacket(videoBuffer, 0, continuity);
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
                        TSPacket packet = new TSPacket(audioBuffer, 1, continuity);
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
}
