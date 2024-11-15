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

    public static Verb getFrom(Class<? extends Annotation> annotation) {
        if (annotation == Get.class) {
            return GET;
        } else if (annotation == Post.class) {
            return POST;
        } else {
            return null;
        }
    }
}
