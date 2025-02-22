package controller;

import annotation.*;
import servlet.*;
import util.*;

@MyAnnotation("controller")
public class Controller{
    int id;

    @Verb.Get 
    @Url("/controller/test")
    public String testGet() {
        return "Test Result";
    }

    @Verb.Post
    @Url("/controller/test")
    public String testPost() {
        return "Test Result Post";
    }

    @Verb.Post
    @Url("/hello/hello")
    public String testt() {
        return "Hello";
    }

    @Verb.Get
    @Url("/upload/form")
    public ModelView UploadForm(){
        String url = "../upload.jsp";
        ModelView mv = new ModelView(url);        
        return mv;
    }
    
    @Verb.Post
    @Url("/upload/file")
    public String UploadDo( @AnnotationParameter(name = "file") FileUpload file ) {
        System.out.println(file.getName());
        System.out.println(file.getPath());
        return file.getName();
    }

}
