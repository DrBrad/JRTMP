package unet.jrtmp;

import unet.jrtmp.stream.StreamManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class RtmpServer {

    private int port;
    private ServerSocket server;
    private Socket socket;

    private StreamManager streamManager;

    public RtmpServer(){
        this(0);
    }

    public RtmpServer(int port){
        this.port = port;
        streamManager = new StreamManager();
    }

    public void start()throws IOException {
        server = new ServerSocket(port);
        System.out.println("Server started on port: "+server.getLocalPort());

        while((socket = server.accept()) != null){
            new RtmpSocket(socket, streamManager).start();
            break;
        }

        server.close();
    }
}
