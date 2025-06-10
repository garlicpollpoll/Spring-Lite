package com.springlite.framework.aop;

import java.lang.reflect.Method;
import java.util.List;

/**
 * A concrete implementation of ProceedingJoinPoint for around advice.
 */
public class MethodProceedingJoinPoint extends MethodJoinPoint implements ProceedingJoinPoint {
    
    private final List<AdviceMetadata> adviceChain;
    private final int currentAdviceIndex;
    private Object[] currentArgs;
    
    public MethodProceedingJoinPoint(Method method, Object[] args, Object target, Object proxy,
                                   List<AdviceMetadata> adviceChain, int currentAdviceIndex) {
        super(method, args, target, proxy);
        this.adviceChain = adviceChain;
        this.currentAdviceIndex = currentAdviceIndex;
        this.currentArgs = args != null ? args.clone() : new Object[0];
    }
    
    @Override
    public Object proceed() throws Throwable {
        return proceed(currentArgs);
    }
    
    @Override
    public Object proceed(Object[] args) throws Throwable {
        this.currentArgs = args != null ? args.clone() : new Object[0];
        
        // If we've executed all around advice, call the target method
        if (currentAdviceIndex >= adviceChain.size()) {
            return invokeTargetMethod();
        }
        
        // Get the next around advice in the chain
        AdviceMetadata advice = adviceChain.get(currentAdviceIndex);
        
        if (advice.getType() != AdviceType.AROUND) {
            // Skip non-around advice and continue to next
            return new MethodProceedingJoinPoint(getMethod(), currentArgs, getTarget(), 
                    getThis(), adviceChain, currentAdviceIndex + 1).proceed();
        }
        
        try {
            // Create next join point for the chain
            ProceedingJoinPoint nextJoinPoint = new MethodProceedingJoinPoint(
                    getMethod(), currentArgs, getTarget(), getThis(), 
                    adviceChain, currentAdviceIndex + 1);
            
            // Invoke the around advice with the next join point
            return advice.invoke(nextJoinPoint);
        } catch (Exception e) {
            if (e.getCause() instanceof Throwable) {
                throw (Throwable) e.getCause();
            }
            throw e;
        }
    }
    
    @Override
    public Object[] getArgs() {
        return currentArgs.clone();
    }
    
    /**
     * Invokes the actual target method.
     */
    private Object invokeTargetMethod() throws Throwable {
        try {
            Method method = getMethod();
            method.setAccessible(true);
            return method.invoke(getTarget(), currentArgs);
        } catch (Exception e) {
            if (e.getCause() instanceof Throwable) {
                throw (Throwable) e.getCause();
            }
            throw e;
        }
    }
}