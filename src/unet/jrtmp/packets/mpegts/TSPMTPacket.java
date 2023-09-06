package unet.jrtmp.packets.mpegts;

import java.util.zip.CRC32;

import static unet.jrtmp.packets.mpegts.TSPacketManager.TS_PACKET_SIZE;

public class TSPMTPacket extends TSPacket {

    /*
    ==============================================================================
    | PADDING | PAT HEADER | Scrambling | Adaptation | Continuity |
    |---------|-----|-------|----------|-----|------------|------------|------------|
    |  0   0  |   VERSION     | PID |      00    |     00     |  Counter   |
    ==============================================================================
    */

    @Override
    public byte[] getEncoded(){
        byte[] packet = super.getEncoded();

        //PADDING
        packet[4] = 0x00; //Padding
        packet[5] = 0x00; //Padding


        //PAT HEADER
        packet[6] = (byte) 0xB0; // PAT version number
        packet[7] = 0x0D; // PAT section number
        packet[8] = 0x00; // PAT program number

        packet[9] = 0x01; // Reserved
        packet[10] = 0x00; // Reserved
        packet[11] = 0x00; // Reserved


        //PROGRAM
        // Add the PID for the audio stream.
        packet[12] = (byte) 0x00;
        packet[13] = (byte) 0x01;

        // Add the PID for the video stream.
        packet[14] = (byte) 0x01;
        packet[15] = (byte) 0x00;


        //CRC CHECKSUM
        int crc32 = (int) calculateCRC32(packet, 5, 9);
        packet[16] = (byte) ((crc32 >> 24) & 0xFF);
        packet[17] = (byte) ((crc32 >> 16) & 0xFF);
        packet[18] = (byte) ((crc32 >> 8) & 0xFF);
        packet[19] = (byte) (crc32 & 0xFF);

        for(int i = 20; i < TS_PACKET_SIZE; i++){
            packet[i] = (byte) 0xFF;
        }

        return packet;
    }

    private long calculateCRC32(byte[] data, int start, int end){
        CRC32 crc32 = new CRC32();
        crc32.update(data, start, end - start + 1);
        return crc32.getValue();
    }
}
