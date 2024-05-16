package annotation; 

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME) 
public @interface MyAnnotation {
    String value();
}