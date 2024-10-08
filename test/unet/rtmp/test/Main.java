package unet.rtmp.test;

import unet.jrtmp.RtmpServer;
import unet.jrtmp.packets.mpegts.TSPacketManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class Main {

    //ADD STREAM MANAGER
    //EVERY TIME WE SEE WRITE / FLUSH (OBJECT) > MEANS CHUNK ENCODE WITH OBJECT IE MESSAGE

    //MAKE TS FILES FOR CHECK
    //ffmpeg -i "/home/brad/Videos/stream.mp4" -codec: copy -start_number 0 -hls_time 10 -hls_list_size 0 -f hls output.m3u8

    //STREAM RTMP
    //ffmpeg -re -i "/home/brad/Videos/stream.mp4" -c:v libx264 -c:a aac -f flv rtmp://127.0.0.1/live

    public static void main(String[] args)throws Exception {
        RtmpServer server = new RtmpServer(1935);
        //server.start();

        //TSPacketManager pm = new TSPacketManager();

        File f = new File("/home/brad/Downloads/test.flv");
        //File f = new File("/home/brad/Videos/stream/output0.ts");
        InputStream in = new FileInputStream(f);

        byte[] b = new byte[188];
        in.read(b);
        System.out.println(hex(b));

        b = new byte[188];
        in.read(b);
        System.out.println(hex(b));
        /*
        TSPacketManager pm = new TSPacketManager();
        pm.add("H".getBytes());
        pm.add("EL".getBytes());
        pm.add("LO".getBytes());
        pm.add("HELLO WORLD".getBytes());
        pm.add(" A".getBytes());
        pm.add("SDASDAS".getBytes());


        for(byte[] segment : pm.getSegments()){
            System.out.println("SEGMENT: "+new String(segment));
        }
        */
    }

    public static String hex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte aByte : bytes) {
            result.append(String.format("%02x ", aByte));
            // upper case
            // result.append(String.format("%02X", aByte));
        }
        return result.toString();
    }
}