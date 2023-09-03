package unet.rtmp.test;

import unet.jrtmp.RtmpServer;
import unet.jrtmp.packets.TSPacketManager;

public class Main {

    //ADD STREAM MANAGER
    //EVERY TIME WE SEE WRITE / FLUSH (OBJECT) > MEANS CHUNK ENCODE WITH OBJECT IE MESSAGE

    public static void main(String[] args)throws Exception {
        RtmpServer server = new RtmpServer(1935);
        server.start();

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
}