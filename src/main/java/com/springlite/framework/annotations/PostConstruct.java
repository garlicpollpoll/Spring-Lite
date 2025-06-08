package com.springlite.framework.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @PostConstruct annotation for lifecycle callback methods
 * Similar to Jakarta EE's @PostConstruct annotation
 * 
 * The method marked with this annotation will be called after dependency injection
 * is done to perform any initialization.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PostConstruct {
} 