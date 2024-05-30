package servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.*;

import java.net.URL;

import java.lang.reflect.Method;

import java.io.PrintWriter;
import java.io.IOException;
import java.io.File;

import java.util.*;

import annotation.*;
import util.*;

public class FrontController extends HttpServlet {    

    public String controller_package;
    public List<Class<?>> listeController;
    public HashMap<String, Mapping> urlMapping = new HashMap<String, Mapping>();

    @Override
    public void init(ServletConfig config) throws ServletException {
        try{
            super.init(config);

            /**************** Prendre tout les contrôleurs  ****************/
            ServletContext context = config.getServletContext();
            this.controller_package = context.getInitParameter("base_package");     // nom package des contrôleurs dans web.xml 
            this.listeController = Util.getControllerClasses(controller_package);   // appel du fonction

            /**************** Creation urlMapping  ****************/
            String controllerName = ""; 
            String methodName = ""; 
            List<Method> listMethod = new ArrayList<>();

            // ajouter dans urlMapping(annotation.value, Mapping) 
            for (Class clazz : this.listeController) {
                controllerName = clazz.getSimpleName();
                listMethod.addAll( Util.findMethodsWithAnnotation(clazz));  //obtention des methodes annotées

                // prendre les methodName et value de l'annotation
                for (Method method : listMethod) {
                    methodName = method.getName();

                    // value de l'annotation
                    Get getAnnotation = method.getAnnotation(Get.class);
                    String url = getAnnotation.value(); 

                    this.urlMapping.put(url, new Mapping(controllerName, methodName));  //ajout dans urlMapping
                }
                // effacer les donnees 
                listMethod.clear();
            }
        }catch( Exception e ){
            e.printStackTrace();
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain");
        
        /**************** Affficher contrôleurs  ****************/        
        String url_typed = request.getRequestURL().toString();
        PrintWriter out = response.getWriter();
        out.println("Liste des contrôleurs : ");
        for (Class<?> controllerClass : this.listeController) {
            out.println(controllerClass.getName());
        }
    
        out.println("\n");    

        /**************** Affficher methode associée  ****************/
        String link = Util.getWords(url_typed, 4);  // prendre l'url a partir du 4eme "/"
        link = "/" + link;
        String link_result = Util.findTheRightMethod(link, this.urlMapping); // prendre value de l'annotation        

        if( link_result != null ){
            String className = Util.getWordAfterNthSlash(link_result, 3);
            String methodName = Util.getWordAfterNthSlash(link_result, 4);            

            out.println("Link typed : " + link);   
            out.println("Class name : " + className);   
            out.println("Method name : " + methodName);

            try{
                Class<?> classeCible = Class.forName( this.controller_package + "." + className);            
                Method maMethode = classeCible.getMethod(methodName);
                Object instance = classeCible.newInstance();
                String resultat = (String)maMethode.invoke(instance);
                out.println("Method return : " + resultat);
            }catch(Exception e ){
                out.println(e);
            }


        }else{ out.println("Il n'y a pas de methode associée a cet url"); }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request,response);        
    }
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request,response);        
    }    

    public void displayMappings(PrintWriter out) {
        for (Map.Entry<String, Mapping> entry : urlMapping.entrySet()) {
            String url = entry.getKey();
            Mapping mapping = entry.getValue();
            out.println("URL: " + url + ", ClassName: " + mapping.getClassName() + ", MethodName: " + mapping.getMethodName());
        }
    }
}