package unet.jrtmp.stream;

import unet.jrtmp.rtmp.messages.AudioMessage;
import unet.jrtmp.rtmp.messages.RtmpMediaMessage;
import unet.jrtmp.rtmp.messages.VideoMessage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Stream {

    private StreamName name;
    private List<RtmpMediaMessage> content;
    private int videoTimestamp, audioTimestamp, obsTimeStamp;
    private Map<String, Object> metadata;

    private OutputStream out;

    //EXTEND FOR VIDEO / AUDIO...

    public Stream(StreamName name){
        this.name = name;
        content = new ArrayList<>();
        try{
            out = new FileOutputStream(new File("/home/brad/Downloads/test.ts"));
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    //public void setBitRate(double bitrate){
    //    this.bitrate = bitrate;
    //}

    public synchronized void add(RtmpMediaMessage message){
        if(name.isObsClient()){
            if(message.getTimestamp() != -1){
                obsTimeStamp = message.getTimestamp();

            }else if(message.getTimestampDelta() != -1){
                obsTimeStamp += message.getTimestampDelta();
            }

            message.setTimestamp(obsTimeStamp);
            if(message instanceof VideoMessage){
                message.setTimestampDelta(obsTimeStamp - videoTimestamp);
                videoTimestamp = obsTimeStamp;
            }else if(message instanceof AudioMessage){
                message.setTimestampDelta(obsTimeStamp - audioTimestamp);
                audioTimestamp = obsTimeStamp;
            }

        }else{
            if(message instanceof VideoMessage){
                VideoMessage vm = (VideoMessage) message;
                if(vm.getTimestamp() != -1){
                    // we may encode as FMT1 ,so we need timestamp delta
                    vm.setTimestampDelta(vm.getTimestamp() - videoTimestamp);
                    videoTimestamp = vm.getTimestamp();

                }else if(vm.getTimestampDelta() != -1){
                    videoTimestamp += vm.getTimestampDelta();
                    vm.setTimestamp(videoTimestamp);
                }
            }

            if(message instanceof AudioMessage){
                AudioMessage am = (AudioMessage) message;
                if(am.getTimestamp() != -1){
                    am.setTimestampDelta(am.getTimestamp() - audioTimestamp);
                    audioTimestamp = am.getTimestamp();

                }else if(am.getTimestampDelta() != -1){
                    audioTimestamp += am.getTimestampDelta();
                    am.setTimestamp(audioTimestamp);
                }
            }
        }

        content.add(message);

        //TRY SAVING AS A FILE...

        //WE WILL LIKELY NEED TO DO SOME HEADER BS...

        try{

        System.out.println("BITRATE: "+metadata.get("videodatarate"));
        out.write(message.raw());
        out.flush();

        }catch(IOException e){
            e.printStackTrace();
        }

        //10 SECOND TS =



    }

    public void setMetadata(Map<String, Object> metadata){
        this.metadata = metadata;
    }
}
