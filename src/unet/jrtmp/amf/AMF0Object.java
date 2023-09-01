package unet.jrtmp.amf;

import java.util.LinkedHashMap;

public class AMF0Object extends LinkedHashMap<String, Object> {

    private static final long serialVersionUID = 1L;

    public AMF0Object addProperty(String key,Object value){
        put(key, value);
        return this;
    }
}
