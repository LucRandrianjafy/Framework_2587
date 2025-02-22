package servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
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
    public String user_role;
    public List<Class<?>> listeController;
    public HashMap<String, Mapping> urlMapping = new HashMap<String, Mapping>();
    public String previousUrl = null;

    @Override
    public void init(ServletConfig config) throws ServletException {
        try{
            super.init(config);

            /**************** Prendre tout les contrôleurs  ****************/
            ServletContext context = config.getServletContext();
            this.controller_package = context.getInitParameter("base_package");     // nom package des contrôleurs dans web.xml         
            this.user_role = context.getInitParameter("user_role");     // nom package des contrôleurs dans web.xml         
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

                        Url urlAnnotation = method.getAnnotation(Url.class);
                        String url = urlAnnotation.value();
                        
                        // MAPPING
                        Set<VerbAction> verbActions = new HashSet<>();
                        VerbAction vbAction = new VerbAction();
                        Verb verb Verb.getFrom(Verb.Get.class);

                        vbAction.add(verb, method);
                        verbActions.add(vbAction);
                        
                        this.urlMapping.put(url, new Mapping(controllerName, methodName, parameterTypes, verbActions));
                        // if (urlMapping.containsKey(url)) {                        
                        //     throw new IllegalArgumentException("Duplicate URL in urlMapping : " + url);
                        // } else {
                        // }
                    }

                    listMethod.clear();
                }
            }
        }catch( Exception e ){
            e.printStackTrace();
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain");

        HttpSession session = request.getSession(false);
        String authorization = "";
        if (session != null) {
            authorization = (String) session.getAttribute(this.user_role);
            System.out.println(authorization);
        }

        String url_typed = request.getRequestURL().toString(); // Capturer l'URL actuelle
        boolean success = false; // Indicateur pour gérer la mise à jour de previousUrl
        String link = Util.getWords(url_typed, 4);  // Extraire l'URL à partir du 4ème "/"
        link = "/" + link;

        try {
            PrintWriter out = response.getWriter();

            /**************** Afficher méthode associée ****************/

            // Trouver la méthode correspondante
            String link_result = Util.findTheRightMethod(link, this.urlMapping, request.getMethod(), response);
            Mapping mapping = Util.getMapping(link, this.urlMapping);

            if (link_result != null) {
                String className = Util.getWordAfterNthSlash(link_result, 3);
                String methodName = Util.getWordAfterNthSlash(link_result, 4);

                // Charger la classe cible et récupérer la méthode correspondante
                Class<?> classeCible = Class.forName(this.controller_package + "." + className);
                Method maMethode = classeCible.getMethod(methodName, mapping.getParameterTypes());
                Object instance = classeCible.newInstance();

                // Préparer les paramètres de la méthode
                Object[] params = Util.getMethodParams(maMethode, previousUrl, request, response);
                System.out.println(previousUrl);

                // Invoquer la méthode cible
                Object result = maMethode.invoke(instance, params);

                // Throws si non autorise
                Authorization.checkAuthorization(maMethode, authorization);

                Util.dispatchData(result, response, request, out, maMethode);

                success = true;
            }
        } catch (Exception e) {
            response.getWriter().write(e.getMessage());
            e.printStackTrace();
        } finally {
            if (success) {
                previousUrl = link;
            }
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {        
        processRequest(request, response);
    }    
    
}