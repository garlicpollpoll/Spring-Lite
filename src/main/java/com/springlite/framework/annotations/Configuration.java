package com.springlite.framework.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Configuration annotation indicating that a class declares one or more @Bean methods
 * Similar to Spring Framework's @Configuration annotation
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component // @Configuration is a specialization of @Component
public @interface Configuration {
    
    /**
     * The value may indicate a suggestion for a logical component name,
     * to be turned into a Spring bean in case of an autodetected component.
     */
    String value() default "";
    
    /**
     * Specify whether @Bean methods should get proxied in order to enforce
     * bean lifecycle behavior, e.g. to return shared singleton bean instances
     * even in case of direct @Bean method calls.
     */
    boolean proxyBeanMethods() default true;
} 