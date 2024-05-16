package servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.*;

import java.net.URL;

import java.io.PrintWriter;
import java.io.IOException;
import java.io.File;

import java.util.*;

import annotation.*;

public class FrontController extends HttpServlet {    

    public String controller_package;
    public List<Class<?>> listeController;

    @Override
    public void init(ServletConfig config) throws ServletException {
        try{
            super.init(config);

            ServletContext context = config.getServletContext();
            this.controller_package = context.getInitParameter("base_package");            
            this.listeController = getControllerClasses(controller_package);

        }catch( Exception e ){
            e.printStackTrace();
        }
    }

    public List<Class<?>> getControllerClasses(String packageName) 
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

        return controllers;
    }

    private List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
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

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain");
        
        String url = request.getRequestURL().toString();
        PrintWriter out = response.getWriter();
        out.println("Liste des controllers : ");

        for (Class<?> controllerClass : this.listeController) {
            out.println(controllerClass.getName());
        }
        
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request,response);        
    }
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request,response);        
    }    
}