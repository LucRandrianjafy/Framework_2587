package util;

import java.util.HashMap;

public class ModelView{
    public String url;
    public HashMap<String, Object> data ;

    public ModelView(String url){
        setUrl(url);
        this.data = new HashMap<String, Object>();
    }    

    public HashMap<String, Object> getData(){
        return this.data;
    }

    public String getUrl(){
        return this.url;
    }

    public void setUrl(String url){
        this.url = url;
    }

    public void add( String key, Object value  ){
        this.data.put(key, value);
    }

}