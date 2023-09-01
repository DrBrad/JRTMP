package unet.jrtmp.stream;

import java.util.concurrent.ConcurrentHashMap;

public class StreamManager {

    private ConcurrentHashMap<StreamName, Stream> streams = new ConcurrentHashMap<>();

    public void newStream(StreamName streamName,Stream s){
        streams.put(streamName, s);
    }

    public boolean exist(StreamName streamName){
        return streams.containsKey(streamName);
    }

    public Stream getStream(StreamName streamName){
        return streams.get(streamName);
    }

    public void remove(StreamName streamName){
        streams.remove(streamName);
    }
}
