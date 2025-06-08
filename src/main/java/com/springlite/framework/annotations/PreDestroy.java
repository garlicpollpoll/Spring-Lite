package com.springlite.framework.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @PreDestroy annotation for lifecycle callback methods
 * Similar to Jakarta EE's @PreDestroy annotation
 * 
 * The method marked with this annotation will be called when the bean instance
 * is being removed from the container.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PreDestroy {
} 