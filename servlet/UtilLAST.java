package util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.servlet.*;

import java.util.*;

import java.net.URL;

import java.io.PrintWriter;
import java.io.IOException;
import java.io.File;
import java.io.InputStream;

import annotation.*;
import servlet.*;
import session.*;

import com.google.gson.Gson;

public class UtilLAST{

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

    // Les classes controleurs
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

    // Les methodes qui sont annotees URL 
    public static List<Method> findMethodsWithAnnotation(Class<?> clazz) {
        List<Method> annotatedMethods = new ArrayList<>();
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Url.class)) {
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
    public static String findTheRightMethod(String link, HashMap<String, Mapping> urlMapping, String verbStr) {
        for (Map.Entry<String, Mapping> entry : urlMapping.entrySet()) {
            String key = entry.getKey();
            Mapping mapping = entry.getValue();
            Set<VerbAction> vbActions = mapping.getVerbAction();
            verbStr = verbStr.toUpperCase();

            // boucle pour voir si verb dans VerbAction = verb
            for ( VerbAction vbAction : vbActions ){
                Verb verb = vbAction.getVerb();                        
                if (link.contains(key) && verbStr.equals( verb.name().toUpperCase() )) {
                    return link + "/" + mapping.getClassName() + "/" + mapping.getMethodName();
                }
            }
        }
        throw new IllegalArgumentException("URL not found");
    }

    public static Mapping getMapping(String link, HashMap<String, Mapping> urlMapping) {
        for (Map.Entry<String, Mapping> entry : urlMapping.entrySet()) {
            String key = entry.getKey();
            Mapping mapping = entry.getValue();
            if (link.contains(key)) {
                return mapping;
            }
        }
        throw new IllegalArgumentException("URL not foundd");        
    }

    public static void dispatchData (Object result , HttpServletResponse response, HttpServletRequest request,  PrintWriter out, Method method)  throws ServletException, IOException{        
        try{            
            // JSON
            if( method.isAnnotationPresent(RestAPI.class) ){                                
                response.setContentType("application/json");
                if( result instanceof String ){                    
                    out.println(new Gson().toJson(result));
                }else if( result instanceof ModelView ){
                    ModelView mv = ((ModelView) result);       
                    out.println(new Gson().toJson(mv.getData()));
                }else{
                    throw new IllegalArgumentException("JSON Another type return");
                }
            }
            else{
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
        }catch(Exception e ) {            
            e.printStackTrace();
            throw new IllegalArgumentException("ERROR DISPACTH DATA");
        }
        
    }

    // prendre les mots apres "?" 
    public static String getWordsAfterQst(String input) {
        String[] parts = input.split("\\?");
        if (parts.length > 1) {
            return parts[1].trim();
        } else {
            return "";
        }
    }

    public static Object typage(String paramValue, String paramName, Class paramType) {
        Object o = null;

        if (paramType == Date.class || paramType == java.sql.Date.class) {
            try {
                o = java.sql.Date.valueOf(paramValue);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid date format for parameter: " + paramName);
            }
        } else if (paramType == int.class) {
            o = Integer.parseInt(paramValue);
        } else if (paramType == double.class) {
            o = Double.parseDouble(paramValue);
        } else if (paramType == boolean.class) {
            o = Boolean.parseBoolean(paramValue);
        } else {
            o = paramValue;
        }
        return o;
    }



    public static Object[] getMethodParams(Method method, HttpServletRequest request) throws IllegalArgumentException, IOException, ServletException {
        Parameter[] parameters = method.getParameters();
        Object[] methodParams = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Class<?> paramType = parameters[i].getType();
            String paramName = "";

            if( paramType == MySession.class ){
                methodParams[i] = new MySession( request.getSession() );          
            }
            else{
                if (parameters[i].isAnnotationPresent(AnnotationParameter.class)) {
                    paramName = parameters[i].getAnnotation(AnnotationParameter.class).name();
                    System.out.println(paramName);
                } else {
                    throw new IllegalArgumentException("Il n y a pas d annotation . ETU2587");
                }


                // Si le type du paramètre est un objet complexe (non primitif et non String)
                if (!paramType.isPrimitive() && !paramType.equals(String.class)) {
                    System.out.println("non primitif");
                    try {
                        if (paramType.equals(FileUpload.class)){
                            System.out.println("equals FileUpload");
                            methodParams[i] = handleFileUpload(request, paramName);
                        } else{
                            Object paramObject = paramType.getDeclaredConstructor().newInstance();
                            // Field[] fields = paramType.getDeclaredFields();
                            
                            // for (Field field : fields) {
                            //     String fieldName = field.getName();
                            //     String fieldValue = request.getParameter(paramName + "." + fieldName);  // prendre les get dans url
                            //     if (fieldValue != null) {
                            //         field.setAccessible(true);
                            //         Object typedValue = typage(fieldValue, fieldName, field.getType()); // (valeur, nom, type)
                            //         field.set(paramObject, typedValue);
                                    
                            //         /*Method m = paramObject.getClass().getMethod(setSetters(fieldName), field.getType());
                            //         m.invoke(paramObject, typedValue);*/
                            //     }
                            // }
                            methodParams[i] = paramObject;
                        }

                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        throw new IllegalArgumentException("Error creating parameter object: " + paramName, e);
                    }
                } else {
                    System.out.println("else primitif");
                    String paramValue = request.getParameter(paramName);
                    if (paramValue == null) {
                        throw new IllegalArgumentException("Missing parameter: " + paramName);
                    }
                    methodParams[i] = typage(paramValue, paramName, paramType);
                }
            }
        }
            
        return methodParams; 
    }

    public static String extractFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        String[] items = contentDisp.split(";");
        for (String item : items) {
            if (item.trim().startsWith("filename")) {
                String fileName = item.substring(item.indexOf('=') + 2, item.length() - 1);
                return fileName;
            }
        }
        return "";
    }


    public static FileUpload handleFileUpload(HttpServletRequest request, String inputFileParam) {
        try {
            System.out.println("handleFileUpload");
            Part filePart = request.getPart(inputFileParam);
            String fileName = extractFileName(filePart);
            byte[] fileContent = filePart.getInputStream().readAllBytes();

            String uploadDir = request.getServletContext().getRealPath("") + "uploads/" + fileName;
            System.out.println("upload = " + uploadDir);

            File uploadFolder = new File(uploadDir);
            if (!uploadFolder.exists()) {
                uploadFolder.mkdirs();
            }

            String uploadPath = uploadDir + File.separator + fileName;
            System.out.println("upload path = " + uploadPath);

            filePart.write(uploadPath);

            return new FileUpload(fileName, uploadPath, fileContent);
        } catch (IOException | ServletException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Méthode pour vérifier si un email est valide
    private static boolean isValidEmail(String email) {
        // Vous pouvez utiliser une expression régulière simple ou une bibliothèque dédiée pour valider l'email
        return email != null && email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
    }

    public static String setSetters(String name) {
        return "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

}