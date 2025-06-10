package com.springlite.framework.aop.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @After annotation for declaring after (finally) advice
 * Similar to Spring Framework's @After annotation
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface After {
    
    /**
     * The pointcut expression or named pointcut.
     * This advice will run whether the method completes normally or throws an exception.
     */
    String value();
    
    /**
     * An optional argument names specification for the advice.
     */
    String argNames() default "";
} 