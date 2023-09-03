package unet.jrtmp.stream;

import java.util.concurrent.ConcurrentHashMap;

public class StreamManager {

    private ConcurrentHashMap<String, Stream> streams;

    public StreamManager(){
        streams = new ConcurrentHashMap<>();
    }

    public void add(String name, Stream stream){
        streams.put(name, stream);
    }

    public Stream get(String name){
        return streams.get(name);
    }

    public boolean contains(String name){
        return streams.contains(name);
    }

    public void remove(String name){
        streams.remove(name);
    }
}
