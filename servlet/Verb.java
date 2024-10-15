package annotation; 

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME) 
public @interface Get {
    String value();
}

public @interface Post {
    String value();
}