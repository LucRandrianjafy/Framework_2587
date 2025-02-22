package util;

import java.lang.reflect.Method;
import javax.servlet.ServletException;
import annotation.Auth;

public class Authorization {
    public static void checkAuthorization(Method maMethode, String authorization) throws ServletException {
        // Vérifier si l'annotation @Auth est présente sur la méthode
        Auth authAnnotation = maMethode.getAnnotation(Auth.class);

        if (authAnnotation != null) {
            String requiredRole = authAnnotation.role(); // Rôle requis

            // Vérifier si l'utilisateur est autorisé
            if (authorization == null || authorization.isEmpty()) {
                throw new ServletException("Accès refusé : vous n'êtes pas autorisé.");
            } else if (!authorization.equals(requiredRole)) {
                throw new ServletException("Votre rôle '" + authorization + "' ne correspond pas au rôle requis '" + requiredRole + "'.");
            }
        }
    }
}
