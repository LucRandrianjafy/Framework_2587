package controller;

import annotation.*;
import util.*;
import session.*;

@MyAnnotation("controller")
public class Employe{

    
    String name;
    String email;
    int age;

    public Employe(){}

    public Employe(String name, int age){
        this.name = name;
        this.age = age;
    }

    public Employe(String name, String email){
        this.name = name;
        this.email = email;
        this.age = age;
    }

    public void setAge(int age){
        this.age = age;
    }

    public int getAge(){
        return this.age;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getName(){
        return this.name;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public String getEmail(){
        return this.email;
    }

    /* EMPLOYE */
    @Verb.Get
    @Url("/controller/emp")
    public ModelView UploadForm(){
        String url = "../form.jsp";
        ModelView mv = new ModelView(url);        
        return mv;
    }

    @Verb.Get
    @Url("/controller/emp2")
    public ModelView test(){
        String url = "/hello/hello";
        ModelView mv = new ModelView(url);
        return mv;
    }

    @Verb.Post
    @Url("/controller/getEmpName")    
    public String getEmpName(@Validation.NotNull(message = "Username cannot be null") @AnnotationParameter(name = "Employe.name") String name, @Validation.Email(message = "Email is invalid") @AnnotationParameter(name = "Employe.email") String mail, @Validation.NotNull(message = "Age cannot be null") @AnnotationParameter(name = "Employe.age") String age ) {   
        Employe emp = new Employe(name, mail);     
        System.out.println(emp.getName());
        System.out.println(emp.getEmail());
        return emp.getName();
    }

    /* SESSION */
    @Verb.Get
    @Url("/login/session")
    public ModelView createSession( MySession session) {          
        int age = 20; 
        session.addAttribute("Luc", age);
        String role = "admin"; 
        session.addAttribute("user_role", role);
        return form();  // appel du fonction
    }

    @Verb.Get
    @Url("/login/form") // appeler par createSession()
    public ModelView form(){
        String url = "../login.jsp";
        ModelView mv = new ModelView(url);        
        return mv;
    }

    @Verb.Get
    @Auth(role="admin")
    @Url("/login/verify")
    public ModelView verify( @AnnotationParameter(name = "name") String name, MySession session) {
        int value = 0 ;
        ModelView mv = new ModelView("../info.jsp");
        if( session.getAttribute(name) != null ){
            value = (int)session.getAttribute(name);
        }else{
            name = "iconnu";
            value = 0;
        }
        Employe emp = new Employe(name, value);
        mv.add("user", emp);
        return mv;
    }
}