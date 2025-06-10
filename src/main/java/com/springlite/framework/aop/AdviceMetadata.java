package com.springlite.framework.aop;

import java.lang.reflect.Method;

/**
 * Holds metadata about a single advice method.
 */
public class AdviceMetadata {
    
    private final Method adviceMethod;
    private final Object aspectInstance;
    private final AdviceType type;
    private final PointcutMatcher pointcutMatcher;
    private final String returningParameterName;
    private final String throwingParameterName;
    
    public AdviceMetadata(Method adviceMethod, Object aspectInstance, AdviceType type, 
                         PointcutMatcher pointcutMatcher) {
        this(adviceMethod, aspectInstance, type, pointcutMatcher, null, null);
    }
    
    public AdviceMetadata(Method adviceMethod, Object aspectInstance, AdviceType type, 
                         PointcutMatcher pointcutMatcher, String returningParameterName, 
                         String throwingParameterName) {
        this.adviceMethod = adviceMethod;
        this.aspectInstance = aspectInstance;
        this.type = type;
        this.pointcutMatcher = pointcutMatcher;
        this.returningParameterName = returningParameterName;
        this.throwingParameterName = throwingParameterName;
    }
    
    /**
     * Tests if this advice matches the given method.
     */
    public boolean matches(Method method, Class<?> targetClass) {
        return pointcutMatcher.matches(method, targetClass);
    }
    
    /**
     * Invokes this advice method.
     */
    public Object invoke(Object... args) throws Exception {
        try {
            adviceMethod.setAccessible(true);
            return adviceMethod.invoke(aspectInstance, args);
        } catch (Exception e) {
            if (e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            } else if (e.getCause() instanceof Exception) {
                throw (Exception) e.getCause();
            } else {
                throw e;
            }
        }
    }
    
    public Method getAdviceMethod() {
        return adviceMethod;
    }
    
    public Object getAspectInstance() {
        return aspectInstance;
    }
    
    public AdviceType getType() {
        return type;
    }
    
    public PointcutMatcher getPointcutMatcher() {
        return pointcutMatcher;
    }
    
    public String getReturningParameterName() {
        return returningParameterName;
    }
    
    public String getThrowingParameterName() {
        return throwingParameterName;
    }
    
    @Override
    public String toString() {
        return String.format("%s advice: %s.%s() with pointcut: %s", 
                type, aspectInstance.getClass().getSimpleName(), 
                adviceMethod.getName(), pointcutMatcher.getExpression());
    }
} 