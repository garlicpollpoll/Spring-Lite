package com.springlite.framework.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Bean annotation for defining Spring beans in configuration classes
 * Similar to Spring Framework's @Bean annotation
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Bean {
    
    /**
     * The name of this bean, or if several names, a primary bean name plus aliases.
     * If left unspecified, the name of the annotated method will be used as the bean name.
     */
    String[] value() default {};
    
    /**
     * The optional name of a method to call on the bean instance during initialization.
     * Not commonly used, given that the method may be called programmatically directly
     * within the body of a Bean-annotated method.
     */
    String initMethod() default "";
    
    /**
     * The optional name of a method to call on the bean instance upon closing the
     * application context, for example a close() method on a DataSource.
     * The method must have no arguments but may throw any exception.
     */
    String destroyMethod() default "";
    
    /**
     * Is this bean a candidate for getting autowired into some other bean?
     * Default is true; set this to false for internal delegates that are not meant
     * to get in the way of beans of the same type in other places.
     */
    boolean defaultCandidate() default true;
} 