package annotation;

import java.lang.annotation.*;

public enum Verb
{
    POST, GET;

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Get {        
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Post {        
    }

    // Méthode pour obtenir le verbe en fonction de l'annotation
    public static Verb getFrom(Class<? extends Annotation> annotation) {
        // Utilisation des 'if' au lieu de 'switch' pour les comparaisons de classes d'annotations
        if (annotation == Get.class) {
            return GET;
        } else if (annotation == Post.class) {
            return POST;
        } else {
            return null; // ou vous pouvez lever une exception IllegalArgumentException
        }
    }
}
