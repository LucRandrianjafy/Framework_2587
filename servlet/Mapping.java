package servlet;

import java.util.Set;

import util.*;
import annotation.*;

public class Mapping{
    String className;
    Set<VerbAction> verbAction;
    String methodName;
    Class[] parameterTypes;

    public Mapping(String className, String methodName) {
        this.className = className;
        this.methodName = methodName;
    }

    public Mapping(String className, String methodName, Class[] parameterTypes) {
        this.className = className;
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
    }
    
    public Mapping(String className, String methodName, Class[] parameterTypes, Set<VerbAction> verbAction ) {
        this.className = className;
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
        this.verbAction = verbAction;
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

    public Set<VerbAction> getVerbAction() {
        return verbAction;
    }

    public void setVerbAction(Set<VerbAction> verbAction) {
        this.verbAction = verbAction;
    }
}