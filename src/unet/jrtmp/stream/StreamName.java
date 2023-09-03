package unet.jrtmp.stream;

public class StreamName {

    private String app, name;
    private boolean obsClient = false;

    public StreamName(String app){
        this.app = app;
    }

    public void setApp(String app){
        this.app = app;
    }

    public String getApp(){
        return app;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public void setObsClient(boolean obsClient){
        this.obsClient = obsClient;
    }

    public boolean isObsClient(){
        return obsClient;
    }

    @Override
    public boolean equals(Object obj){
        if(this == obj){
            return true;
        }

        if(obj == null){
            return false;
        }

        if(getClass() != obj.getClass()){
            return false;
        }

        StreamName other = (StreamName) obj;
        if(app == null){
            if(other.app != null){
                return false;
            }
        }else if(!app.equals(other.app)){
            return false;
        }

        if(name == null){
            if(other.name != null){
                return false;
            }

        }else if(!name.equals(other.name)){
            return false;
        }

        return true;
    }

    @Override
    public int hashCode(){
        int prime = 31;
        int result = 1;
        result = prime * result + ((app == null) ? 0 : app.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }
}
