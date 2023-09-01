package unet.rtmp.test;

import unet.jrtmp.RtmpServer;

public class Main {

    public static void main(String[] args)throws Exception {
        RtmpServer server = new RtmpServer(1935);
        server.start();
    }
}