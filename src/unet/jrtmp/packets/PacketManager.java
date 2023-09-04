package unet.jrtmp.packets;

import unet.jrtmp.rtmp.messages.RtmpMediaMessage;

public abstract class PacketManager {

    public PacketManager(){
    }

    public abstract void add(RtmpMediaMessage message);

    public abstract void write(Packet packet);
}
