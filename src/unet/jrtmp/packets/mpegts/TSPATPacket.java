package unet.jrtmp.packets.mpegts;

import java.util.zip.CRC32;

import static unet.jrtmp.packets.mpegts.TSPacketManager.TS_PACKET_SIZE;

public class TSPATPacket extends TSPacket {

    /*
    ==============================================================================
    | PADDING | PAT HEADER | Scrambling | Adaptation | Continuity |
    |---------|-----|-------|----------|-----|------------|------------|------------|
    |  0   0  |   VERSION     | PID |      00    |     00     |  Counter   |
    ==============================================================================
    */
    public TSPATPacket(int pid, int continuity){
        this.pid = pid;
        this.continuity = continuity;
    }

    @Override
    public byte[] getEncoded(){
        byte[] packet = super.getEncoded();

        int programNumber = 0x01;
        int[] pids = new int[]{ 0x01, 0x02 };

        // Set the padding.
        packet[4] = 0x00;
        packet[5] = 0x00;

        // Set the PAT header.
        packet[6] = (byte) 0xB0; // Table ID (0x00 for PAT), Section length - High byte
        packet[7] = 0x0D; // Section length - Low byte (13 bytes excluding the first 5 bytes)
        packet[8] = 0x00; // Transport Stream ID (16 bits) - High byte
        packet[9] = 0x01; // Transport Stream ID (16 bits) - Low byte
        packet[10] = (byte) 0xC1; // Version number (5 bits), Current/Next indicator (1 bit), Section number (8 bits)
        packet[11] = 0x00; // Last section number (8 bits)

        /*
        packet[6] = (byte) 0xB0; // PAT version number
        packet[7] = (byte) 0x0D; // PAT section number
        packet[8] = (byte) programNumber; // PAT program number

        packet[9] = (byte) 0xC1; // Reserved
        packet[10] = 0x00; // Reserved
        packet[11] = 0x00; // Reserved

        // Add the PIDs for the streams in the program.
        /*
        int skip = 0;
        for(int pid : pids){
            packet[12+pid/8] |= (1 << (pid % 8));
            skip += pid/8;
        }

        System.out.println(skip);
        */

        // Calculate the CRC32 checksum.
        CRC32 crc = new CRC32();
        crc.update(packet, 5, 12);

        // Write the CRC32 checksum to the PAT.
        packet[12] = (byte) (crc.getValue() & 0xFF);
        packet[13] = (byte) ((crc.getValue() >> 8) & 0xFF);
        packet[14] = (byte) ((crc.getValue() >> 16) & 0xFF);
        packet[15] = (byte) ((crc.getValue() >> 24) & 0xFF);



        /*
        //PADDING
        packet[4] = 0x00; //Padding
        packet[5] = 0x00; //Padding


        //PAT HEADER
        packet[6] = (byte) 0xB0; // PAT version number
        packet[7] = 0x0D; // PAT section number
        //packet[8] = 0x00; // PAT program number
        packet[8] = 0x00; // Program number (high byte)
        packet[9] = 0x01; // Program number (low byte)

        packet[10] = (byte) 0xC1; // UNSURE WHAT THIS BYTE IS...

        packet[11] = 0x00; // Reserved
        packet[12] = 0x00; // Reserved
        packet[13] = 0x00; // Reserved

        //PROGRAM
        // Add the PID for the audio stream.
        packet[14] = (byte) 0x01;
        packet[15] = (byte) 0xF0;

        // Add the PID for the video stream.
        packet[16] = (byte) 0x01;
        packet[17] = (byte) 0x2E;


        //CRC CHECKSUM
        int crc32 = (int) calculateCRC32(packet, 0, 16);
        packet[18] = (byte) ((crc32 >> 24) & 0xFF);
        packet[19] = (byte) ((crc32 >> 16) & 0xFF);
        packet[20] = (byte) ((crc32 >> 8) & 0xFF);
        packet[21] = (byte) (crc32 & 0xFF);
        */

        for(int i = 21; i < TS_PACKET_SIZE; i++){
            packet[i] = (byte) 0xFF;
        }

        return packet;
    }

    /*
    private long calculateCRC32(byte[] data, int start, int end){
        CRC32 crc32 = new CRC32();
        crc32.update(data, start, end - start + 1);
        return crc32.getValue();
    }
    */
}
