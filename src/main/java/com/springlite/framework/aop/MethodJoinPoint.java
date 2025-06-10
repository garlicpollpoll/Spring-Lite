package com.springlite.framework.aop;

import java.lang.reflect.Method;

/**
 * A concrete implementation of JoinPoint for method execution join points.
 */
public class MethodJoinPoint implements JoinPoint {
    
    private final Method method;
    private final Object[] args;
    private final Object target;
    private final Object proxy;
    
    public MethodJoinPoint(Method method, Object[] args, Object target, Object proxy) {
        this.method = method;
        this.args = args != null ? args.clone() : new Object[0];
        this.target = target;
        this.proxy = proxy;
    }
    
    @Override
    public Method getMethod() {
        return method;
    }
    
    @Override
    public Object[] getArgs() {
        return args.clone();
    }
    
    @Override
    public Object getTarget() {
        return target;
    }
    
    @Override
    public Object getThis() {
        return proxy;
    }
    
    @Override
    public String getSignature() {
        StringBuilder sb = new StringBuilder();
        sb.append(method.getReturnType().getSimpleName()).append(" ");
        sb.append(method.getDeclaringClass().getSimpleName()).append(".");
        sb.append(method.getName()).append("(");
        
        Class<?>[] paramTypes = method.getParameterTypes();
        for (int i = 0; i < paramTypes.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(paramTypes[i].getSimpleName());
        }
        sb.append(")");
        return sb.toString();
    }
    
    @Override
    public String getKind() {
        return "method-execution";
    }
    
    @Override
    public String toShortString() {
        return String.format("execution(%s.%s(..))", 
                method.getDeclaringClass().getSimpleName(), 
                method.getName());
    }
    
    @Override
    public String toLongString() {
        return String.format("execution(%s)", getSignature());
    }
    
    @Override
    public String toString() {
        return toShortString();
    }
} 