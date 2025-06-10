package com.springlite.framework.aop.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @AfterThrowing annotation for declaring after throwing advice
 * Similar to Spring Framework's @AfterThrowing annotation
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AfterThrowing {
    
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
     * The name of the argument in the advice signature to bind the thrown exception to.
     */
    String throwing() default "";
    
    /**
     * An optional argument names specification for the advice.
     */
    String argNames() default "";
} 