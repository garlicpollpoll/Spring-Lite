package com.springlite.framework.aop.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @AfterReturning annotation for declaring after returning advice
 * Similar to Spring Framework's @AfterReturning annotation
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AfterReturning {
    
    /**
     * The pointcut expression or named pointcut.
     */
    String value() default "";
    
    /**
     * The pointcut expression or named pointcut.
     * Alternative to value().
     */
    String pointcut() default "";
    
    /**
     * The name of the argument in the advice signature to bind the returned value to.
     */
    String returning() default "";
    
    /**
     * An optional argument names specification for the advice.
     */
    String argNames() default "";
} 