package util;

import java.lang.reflect.Method;

import annotation.*;

public class VerbAction {
    private Verb verb;
    private Method method;

    public VerbAction() {}

    public void add(Verb verb, Method method) {
        this.verb = verb;
        this.method = method;
    }

    public Verb getVerb() {
        return verb;
    }

    public Method getMethod() {
        return method;
    }
}
