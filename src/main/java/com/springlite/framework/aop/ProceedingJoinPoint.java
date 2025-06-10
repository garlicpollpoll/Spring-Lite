package com.springlite.framework.aop;

/**
 * Exposes the proceed(..) method in order to support around advice.
 * Similar to AspectJ's ProceedingJoinPoint interface.
 */
public interface ProceedingJoinPoint extends JoinPoint {
    
    /**
     * Proceed with the next advice or target method invocation.
     * 
     * @return the return value of the method, if any
     * @throws Throwable any exception thrown by the target method
     */
    Object proceed() throws Throwable;
    
    /**
     * Proceed with the next advice or target method invocation, 
     * replacing the arguments.
     * 
     * @param args the new arguments to use for the invocation
     * @return the return value of the method, if any
     * @throws Throwable any exception thrown by the target method
     */
    Object proceed(Object[] args) throws Throwable;
} 