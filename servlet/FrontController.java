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
import session.*;

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
            if( this.listeController != null ){
                for (Class clazz : this.listeController) {
                    controllerName = clazz.getSimpleName();
                    listMethod.addAll( Util.findMethodsWithAnnotation(clazz));  //obtention des methodes annotées

                    // prendre les methodName et value de l'annotation
                    for (Method method : listMethod) {
                        methodName = method.getName();

                        Class[] parameterTypes = method.getParameterTypes();

                        // value de l'annotation
                        Get getAnnotation = method.getAnnotation(Get.class);
                        String url = getAnnotation.value(); 

                        if (urlMapping.containsKey(url)) {                        
                            throw new IllegalArgumentException("Duplicate URL in urlMapping : " + url);
                        } else {
                            this.urlMapping.put(url, new Mapping(controllerName, methodName, parameterTypes));
                        }
                    }
                    // effacer les donnees 
                    listMethod.clear();
                }
            }
        }catch( Exception e ){
            e.printStackTrace();
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain");
        
        /**************** Affficher contrôleurs  ****************/        
        try{            

            String url_typed = request.getRequestURL().toString();
            PrintWriter out = response.getWriter();
            
            // out.println("url_typed : " + url_typed);                        

            // out.println("Liste des contrôleurs : ");
            // if( this.listeController != null ){
            //     for (Class<?> controllerClass : this.listeController) {
            //         out.println(controllerClass.getName());
            //     }
            // }        


            /**************** Affficher methode associée  ****************/
            String link = Util.getWords(url_typed, 4);  // prendre l'url a partir du 4eme "/"
            link = "/" + link;

            String link_result = Util.findTheRightMethod(link, this.urlMapping); // prendre value de l'annotation  
            // out.println("link result " + link_result);

            Mapping mapping = Util.getMapping(link, this.urlMapping); // prendre mapping

            // out.println("Liste des methodes : ");
            // displayMappings(out);

            if( link_result != null ){
                String className = Util.getWordAfterNthSlash(link_result, 3);
                String methodName = Util.getWordAfterNthSlash(link_result, 4);            

                // out.println("Link typed : " + link);
                // out.println("Class name : " + className);
                // out.println("Method name : " + methodName);

                Class<?> classeCible = Class.forName( this.controller_package + "." + className);            
                Method maMethode = classeCible.getMethod(methodName, mapping.getParameterTypes());
                Object instance = classeCible.newInstance();

                Object[] params = Util.getMethodParams(maMethode, request);
                Object result = maMethode.invoke(instance, params);
                
                Util.dispatchData(result, response, request, out, maMethode);
            }

        }catch(Exception e ){
            response.getWriter().write(e.getMessage());
        }            
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