package com.springlite.framework.aop.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Pointcut annotation for defining named pointcut expressions
 * Similar to Spring Framework's @Pointcut annotation
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Pointcut {
    
    /**
     * The pointcut expression.
     * Examples:
     * - "execution(* com.xyz.service.*.*(..))" - matches all methods in service package
     * - "execution(public * *(..))" - matches all public methods
     * - "@annotation(org.springframework.transaction.annotation.Transactional)" - matches @Transactional methods
     * - "within(com.xyz.service..*)" - matches methods within service package
     * - "bean(*Service)" - matches methods on beans ending with "Service"
     */
    String value();
    
    /**
     * An optional argument names specification for the pointcut.
     */
    String argNames() default "";
} 