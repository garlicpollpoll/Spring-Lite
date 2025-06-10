package com.springlite.framework.aop;

import java.lang.reflect.Method;

/**
 * Provides access to the context of the join point being advised.
 * Similar to AspectJ's JoinPoint interface.
 */
public interface JoinPoint {
    
    /**
     * Returns the method being executed at this join point.
     */
    Method getMethod();
    
    /**
     * Returns the arguments at this join point.
     */
    Object[] getArgs();
    
    /**
     * Returns the target object.
     * This is the object being proxied.
     */
    Object getTarget();
    
    /**
     * Returns the proxy object.
     */
    Object getThis();
    
    /**
     * Returns a string representation of the join point signature.
     */
    String getSignature();
    
    /**
     * Returns the kind of join point (e.g., "method-execution").
     */
    String getKind();
    
    /**
     * Returns a short string representation of the join point.
     */
    String toShortString();
    
    /**
     * Returns a long string representation of the join point.
     */
    String toLongString();
} 