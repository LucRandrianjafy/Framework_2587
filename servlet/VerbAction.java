package util;

import annotation.*;

public class VerbAction {
    private Verb verb;
    private Method method;

    // Constructeur
    public VerbAction() {}

    // Méthode add pour ajouter un verbe et une méthode
    public void add(Verb verb, Method method) {
        this.verb = verb;
        this.method = method;
    }

    // Accesseurs (getters)
    public Verb getVerb() {
        return verb;
    }

    public Method getMethod() {
        return method;
    }
}
