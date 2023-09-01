package unet.jrtmp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class RtmpServer {

    private int port;
    private ServerSocket server;
    private Socket socket;

    public RtmpServer(){
        this(0);
    }

    public RtmpServer(int port){
        this.port = port;
    }

    public void start()throws IOException {
        server = new ServerSocket(port);
        System.out.println("Server started on port: "+server.getLocalPort());

        while((socket = server.accept()) != null){
            new RtmpSocket(socket).start();
        }

        server.close();
    }
}
