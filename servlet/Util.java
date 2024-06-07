package util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.*;

import java.util.*;

import java.net.URL;

import java.io.PrintWriter;
import java.io.IOException;
import java.io.File;

import annotation.*;
import servlet.*;
 
public class Util{

    // Prendre les Controllers a l aide de l'annotation MyAnnotation
    public static List<Class<?>> getControllerClasses(String packageName) 
        throws ClassNotFoundException, IOException 
    {
        List<Class<?>> controllers = new ArrayList<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;

        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<>();

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }

        for (File directory : dirs) {
            controllers.addAll(findClasses(directory, packageName));
        }

        if(dirs.size() == 0 ){
            throw new IllegalArgumentException("Package name doesn't exist ");
        }else{
            return controllers;
        }
    }

    private static List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        if (!directory.exists()) {
            return classes;
        }

        File[] files = directory.listFiles();
        assert files != null;
        for (File file : files) {
            if (file.isDirectory()) {
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } 
            
            else if (file.getName().endsWith(".class")) {
                Class<?> clazz = Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6));
                if (clazz.isAnnotationPresent(MyAnnotation.class)) {
                    classes.add(clazz);
                }
            }
        }
        return classes;
    }

    // prendre method qui contient un Annotation Get
    public static List<Method> findMethodsWithAnnotation(Class<?> clazz) {
        List<Method> annotatedMethods = new ArrayList<>();
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Get.class)) {
                annotatedMethods.add(method);
            }
        }
        return annotatedMethods;
    }

    // prendre tout les mots A PARTIR DU nieme "/"
    public static String getWords(String input, int n) {
        String[] parts = input.split("/");
        if (n < parts.length) {
            StringBuilder result = new StringBuilder();
            for (int i = n; i < parts.length; i++) {
                result.append(parts[i]);
                if (i < parts.length - 1) {
                    result.append("/");
                }
            }
            return result.toString();
        }
        return "";
    }

    // prendre le mot APRES LE nieme "/"
    public static String getWordAfterNthSlash(String input, int n) {
        String[] parts = input.split("/");
        if (n < parts.length) {
            return parts[n];
        }
        return "";
    }

    // prenre la methode qui contient l'annotation appropriée
    public static String findTheRightMethod(String link, HashMap<String, Mapping> urlMapping) {
        for (Map.Entry<String, Mapping> entry : urlMapping.entrySet()) {
            String key = entry.getKey();
            Mapping mapping = entry.getValue();
            if (link.contains(key)) {
                return link + "/" + mapping.getClassName() + "/" + mapping.getMethodName();
            }
        }
        throw new IllegalArgumentException("URL not found");        
    }

    public static void dispatchData (Object result , HttpServletResponse response, HttpServletRequest request,  PrintWriter out)  throws ServletException, IOException{
        if( result instanceof String ){                    
            out.println("Method return : " + (String) result);
        }else if( result instanceof ModelView ){            
            ModelView mv = ((ModelView) result);
            for (Map.Entry<String, Object> entry : mv.getData().entrySet()) {
                request.setAttribute(entry.getKey(), entry.getValue());
            }              
            RequestDispatcher dispatcher = request.getRequestDispatcher(mv.getUrl());
            dispatcher.forward(request, response);

        }else{
            throw new IllegalArgumentException("Another type return");
        }
    }

}