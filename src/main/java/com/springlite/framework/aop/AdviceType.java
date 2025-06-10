package com.springlite.framework.aop;

/**
 * Enumeration of advice types in Spring AOP.
 */
public enum AdviceType {
    
    /**
     * Before advice - runs before the method execution.
     */
    BEFORE,
    
    /**
     * After (finally) advice - runs after the method execution, 
     * regardless of whether it completed normally or threw an exception.
     */
    AFTER,
    
    /**
     * After returning advice - runs after the method execution 
     * completes normally (without throwing an exception).
     */
    AFTER_RETURNING,
    
    /**
     * After throwing advice - runs after the method execution 
     * exits by throwing an exception.
     */
    AFTER_THROWING,
    
    /**
     * Around advice - surrounds method execution. 
     * Has complete control over method execution.
     */
    AROUND
} 