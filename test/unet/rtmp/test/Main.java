package unet.rtmp.test;

import unet.jrtmp.RtmpServer;

public class Main {

    //ADD STREAM MANAGER

    public static void main(String[] args)throws Exception {
        RtmpServer server = new RtmpServer(1935);
        server.start();
    }
}