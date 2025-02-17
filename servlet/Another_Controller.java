package controller;

import annotation.*;
import util.*;

@MyAnnotation("controller")
public class Another_Controller{
    int id;

    public Another_Controller(){}

    
    @Url("/controller/id")
    public String getString(){
        return "This is the result";
    }    

    public void setId(int id){
        this.id = id;
    }

    public int getId(){
        return this.id;
    }

    @Verb.Get
    @Url("/controller/name")
    public ModelView getName(){

        Another_Controller ac = new Another_Controller();
        ac.setId(19);

        String url = "../test.jsp";
        ModelView mv = new ModelView(url);
        String key = "age";
        mv.add(key, ac);
        return mv;
    }

    @Verb.Get
    @Url("/controller/num")
    public int getNum(){        
        return 100;
    }

    
}

