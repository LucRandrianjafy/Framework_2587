package session;

import javax.servlet.http.HttpSession;

public class MySession {
    private HttpSession session;

    public MySession(HttpSession session) {
        this.session = session;
    }

    // Getter pour HttpSession
    public HttpSession getSession() {
        return session;
    }

    // Setter pour HttpSession
    public void setSession(HttpSession session) {
        this.session = session;
    }

    // Méthode pour ajouter un attribut à la session
    public void addAttribute(String name, Object value) {
        session.setAttribute(name, value);
    }

    // Méthode pour récupérer un attribut de la session
    public Object getAttribute(String name) {
        return session.getAttribute(name);
    }

    // Méthode pour supprimer un attribut de la session
    public void removeAttribute(String name) {
        session.removeAttribute(name);
    }
}