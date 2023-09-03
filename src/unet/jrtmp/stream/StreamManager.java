package unet.jrtmp.stream;

import java.util.concurrent.ConcurrentHashMap;

public class StreamManager {

    private ConcurrentHashMap<StreamName, Stream> streams;

    public StreamManager(){
        streams = new ConcurrentHashMap<>();
    }

    public void add(StreamName name, Stream stream){
        streams.put(name, stream);
    }

    public Stream get(StreamName name){
        return streams.get(name);
    }

    public boolean contains(StreamName name){
        return streams.contains(name);
    }

    public void remove(StreamName name){
        streams.remove(name);
    }
}
