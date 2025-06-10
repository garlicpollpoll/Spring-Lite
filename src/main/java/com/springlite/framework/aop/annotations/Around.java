package com.springlite.framework.aop.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Around annotation for declaring around advice
 * Similar to Spring Framework's @Around annotation
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Around {
    
    /**
     * The pointcut expression or named pointcut.
     * Around advice has complete control over method execution and must
     * call ProceedingJoinPoint.proceed() to continue execution.
     */
    String value();
    
    /**
     * An optional argument names specification for the advice.
     */
    String argNames() default "";
} 