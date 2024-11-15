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
            this.listeController = Util.getControllerClasses(controller_package);                     

            /**************** Creation urlMapping  ****************/
            String controllerName = ""; 
            String methodName = ""; 
            List<Method> listMethod = new ArrayList<>();

            // ajouter dans urlMapping(annotation.value, Mapping) 
            if( this.listeController != null ){
                for (Class clazz : this.listeController) {
                    controllerName = clazz.getSimpleName();
                    listMethod.addAll( Util.findMethodsWithAnnotation(clazz) );  // obtention des methodes annotées URL

                    // prendre les methodName et value de l'annotation
                    for (Method method : listMethod) {
                        methodName = method.getName();

                        Class[] parameterTypes = method.getParameterTypes();

                        // value de l'annotation
                        Url urlAnnotation = method.getAnnotation(Url.class);
                        String url = urlAnnotation.value();
                        
                        // MAPPING
                        Set<VerbAction> verbActions = new HashSet<>();
                        VerbAction vbAction = new VerbAction();
                        Verb verb;

                        if (method.getAnnotation(Verb.Get.class) != null) {
                            verb = Verb.getFrom(Verb.Get.class);
                            vbAction.add(verb, method);
                            verbActions.add(vbAction);
                        } else if (method.getAnnotation(Verb.Post.class) != null) {
                            verb = Verb.getFrom(Verb.Post.class);
                            vbAction.add(verb, method);
                            verbActions.add(vbAction);
                        } else {
                            verb = null;
                        }
                            


                        this.urlMapping.put(url, new Mapping(controllerName, methodName, parameterTypes, verbActions));
                        // if (urlMapping.containsKey(url)) {                        
                        //     throw new IllegalArgumentException("Duplicate URL in urlMapping : " + url);
                        // } else {
                        // }
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
                        
            /**************** Affficher methode associée  ****************/
            String link = Util.getWords(url_typed, 4);  // prendre l'url a partir du 4eme "/"
            link = "/" + link;

            // Mettre en argument request.getMethod()
            String link_result = Util.findTheRightMethod(link, this.urlMapping, request.getMethod()); // prendre value de l'annotation  
            
            Mapping mapping = Util.getMapping(link, this.urlMapping); // prendre mapping
            
            /************   ***************/            
            if( link_result != null ){
                String className = Util.getWordAfterNthSlash(link_result, 3);
                String methodName = Util.getWordAfterNthSlash(link_result, 4);            

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
        processRequest(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {        
        // PrintWriter out = response.getWriter();

        // Object result = request.getAttribute("result");
        // Method maMethode = (Method)request.getAttribute("maMethode");

        // if( result == null ){
        //     Util.dispatchData( result, response, request, out, maMethode );
        // }else{
        //     System.out.println( " No method found with this Verb " );
        // }
        processRequest(request, response);
    }    
    
}