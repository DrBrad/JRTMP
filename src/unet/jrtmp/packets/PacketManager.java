package unet.jrtmp.packets;

import java.util.ArrayList;
import java.util.List;

public abstract class PacketManager {

    //protected List<byte[]> payloads;

    public PacketManager(){
        //payloads = new ArrayList<>();
    }

    //> RTMP DATA TO CREATE PACKET
    /*
    public void add(byte[] payload){
        payloads.add(payload);
    }
    */
    public abstract void add(byte[] payload);

    public abstract void write(Packet packet);
}
