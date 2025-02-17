package annotation;

import java.lang.annotation.*;

public enum Validation {
    EMAIL, NOTNULL;

    @Retention(RetentionPolicy.RUNTIME)
    public @interface NotNull {
        String message() default "Field cannot be null";
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Email {
        String message() default "Invalid email format";
    }

}