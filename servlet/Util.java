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
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;

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
    public static String findTheRightMethod(String link, HashMap<String, Mapping> urlMapping, String verbStr, HttpServletResponse response) {
        for (Map.Entry<String, Mapping> entry : urlMapping.entrySet()) {
            String key = entry.getKey();
            Mapping mapping = entry.getValue();
            Set<VerbAction> vbActions = mapping.getVerbAction();
            verbStr = verbStr.toUpperCase();

            // boucle pour voir si verb dans VerbAction = verb
            for (VerbAction vbAction : vbActions) {
                Verb verb = vbAction.getVerb();                        
                if (link.equals(key) && verbStr.equals(verb.name().toUpperCase())) {
                    return link + "/" + mapping.getClassName() + "/" + mapping.getMethodName();
                }
            }
        }

        // Si aucune correspondance trouvée, retourner 404
        try {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "URL not found");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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

    public static Object[] getMethodParams(Method method, String previousUrl, HttpServletRequest request, HttpServletResponse response)
        throws IllegalArgumentException, IOException, ServletException {
        System.out.println(previousUrl);
        Parameter[] parameters = method.getParameters();
        Object[] methodParams = new Object[parameters.length];
        Map<String, String> validationErrors = new HashMap<>(); // Liste pour collecter les erreurs
        Map<String, String> invalidValues = new HashMap<>(); // Map pour collecter les valeurs invalides

        for (int i = 0; i < parameters.length; i++) {
            Class<?> paramType = parameters[i].getType();
            String paramName = "";

            if (paramType == MySession.class) {
                methodParams[i] = new MySession(request.getSession());
            } else {
                if (parameters[i].isAnnotationPresent(AnnotationParameter.class)) {
                    paramName = parameters[i].getAnnotation(AnnotationParameter.class).name();
                } else {
                    validationErrors.put("Il n'y a pas d'annotation pour ce paramètre. ETU2587", paramName);
                    continue; // On continue pour vérifier les autres paramètres
                }

                try {
                    if (!paramType.isPrimitive() && !paramType.equals(String.class)) {
                        if (paramType.equals(FileUpload.class)) {
                            System.out.println("equals FileUpload");
                            methodParams[i] = handleFileUpload(request, paramName);
                        } else {
                            Object paramObject = paramType.getDeclaredConstructor().newInstance();
                            Field[] fields = paramType.getDeclaredFields();

                            for (Field field : fields) {
                                field.setAccessible(true);
                                String fieldName = field.getName();
                                String fieldValue = request.getParameter(paramName + "." + fieldName);
                                System.out.println("FieldValue = " + fieldValue);

                                invalidValues.put(paramName, fieldValue);
                                try {
                                    validateParameterAnnotations(parameters[i], fieldValue);
                                } catch (IllegalArgumentException e) {
                                    validationErrors.put(paramName, e.getMessage());
                                    System.out.println(e.getMessage());

                                }

                                if (fieldValue != null) {
                                    Object typedValue = typage(fieldValue, fieldName, field.getType());
                                    field.set(paramObject, typedValue);
                                }
                            }
                            methodParams[i] = paramObject;
                        }
                    } else {
                        String paramValue = request.getParameter(paramName);
                        if (paramValue == null) {
                            validationErrors.put(paramName, "Missing parameter: " + paramName);
                            continue;
                        }

                        invalidValues.put(paramName, paramValue);
                        try {
                            validateParameterAnnotations(parameters[i], paramValue);
                        } catch (IllegalArgumentException e) {
                            validationErrors.put(paramName, e.getMessage());
                            System.out.println(e.getMessage());

                        }

                        methodParams[i] = typage(paramValue, paramName, paramType);
                    }
                } catch (Exception e) {
                    validationErrors.put(paramName, "Erreur lors de la création de l'objet pour le paramètre '" + paramName + "': " + e.getMessage());
                }
            }
        }

        // Si des erreurs de validation sont présentes, dispatcher vers l'URL précédente
        if (!validationErrors.isEmpty()) {
            // Ajout des erreurs et valeurs invalides dans les attributs de la requête
            request.setAttribute("validationErrors", validationErrors);
            request.setAttribute("invalidValues", invalidValues);
            RequestDispatcher dispatcher = request.getRequestDispatcher(previousUrl);
            try {
                dispatcher.forward(request, response);
            } catch (ServletException | IOException e) {
                e.printStackTrace();
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erreur lors du traitement des erreurs de validation.");
            }
            return null;
        }

        return methodParams;
    }



    // Fonction de validation des annotations NotNull et Email
    private static void validateParameterAnnotations(Parameter parameter, String paramValue) throws IllegalArgumentException {
        if (parameter.isAnnotationPresent(Validation.NotNull.class)) {
            if (paramValue == null || paramValue.trim().isEmpty()) {
                throw new IllegalArgumentException("Le paramètre '" + parameter.getName() + "' ne peut pas être nul ou vide.");
            }
        }

        if (parameter.isAnnotationPresent(Validation.Email.class)) {
            if (paramValue == null || !isValidEmail(paramValue)) {
                throw new IllegalArgumentException("L'email fourni '" + paramValue + "' est invalide.");
            }
        }
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

    private static boolean isValidEmail(String email) {
        return email != null && email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
    }

    public static String setSetters(String name) {
        return "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

}