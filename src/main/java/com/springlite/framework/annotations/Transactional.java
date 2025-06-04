package com.springlite.framework.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Transactional {
    boolean readOnly() default false;
    String value() default "";
    Propagation propagation() default Propagation.REQUIRED;
    
    enum Propagation {
        REQUIRED, REQUIRES_NEW, SUPPORTS, NOT_SUPPORTED, NEVER, MANDATORY, NESTED
    }
} 