package unet.rtmp.test;

import unet.jrtmp.RtmpServer;

public class Main {

    //ADD STREAM MANAGER
    //EVERY TIME WE SEE WRITE / FLUSH (OBJECT) > MEANS CHUNK ENCODE WITH OBJECT IE MESSAGE

    public static void main(String[] args)throws Exception {
        RtmpServer server = new RtmpServer(1935);
        server.start();
    }
}