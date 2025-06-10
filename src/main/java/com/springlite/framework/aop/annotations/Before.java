package com.springlite.framework.aop.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Before annotation for declaring before advice
 * Similar to Spring Framework's @Before annotation
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Before {
    
    /**
     * The pointcut expression or named pointcut.
     * Examples:
     * - "execution(* com.xyz.service.*.*(..))" - inline pointcut expression
     * - "com.xyz.CommonPointcuts.businessService()" - named pointcut reference
     * - "dataAccessOperation()" - local named pointcut reference
     */
    String value();
    
    /**
     * An optional argument names specification for the advice.
     */
    String argNames() default "";
} 