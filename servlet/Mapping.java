package servlet;

import annotation.*;

public class Mapping{
    String className;
    String methodName;
    Class[] parameterTypes;
    Verb verbAction;

    public Mapping(String className, String methodName) {
        this.className = className;
        this.methodName = methodName;
    }

    public Mapping(String className, String methodName, Class[] parameterTypes) {
        this.className = className;
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
    }

    public Mapping(){}

    public String getClassName() {
        return className;
    }

    public Class[] getParameterTypes() {
        return parameterTypes;        
    }

    public void setParameterTypes( Class[] param ) {
        this.parameterTypes = param;
    }

    public String getMethodName() {
        return methodName;
    }

    public Verb getVerbAction() {
        return verbAction;
    }

    public void setVerbAction( Verb v ) {
        this.verbAction = v;
    }
}