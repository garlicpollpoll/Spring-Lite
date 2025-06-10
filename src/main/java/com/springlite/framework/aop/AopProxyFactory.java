package com.springlite.framework.aop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

/**
 * Factory for creating AOP proxies with full aspect support.
 */
public class AopProxyFactory {
    
    private final List<AspectMetadata> aspects;
    
    public AopProxyFactory() {
        this.aspects = new ArrayList<>();
    }
    
    public void addAspect(AspectMetadata aspect) {
        aspects.add(aspect);
        System.out.println("📎 AOP: Added aspect " + aspect.getAspectClass().getSimpleName());
    }
    
    public Object createProxy(Object target) {
        Class<?> targetClass = target.getClass();
        Class<?>[] interfaces = targetClass.getInterfaces();
        
        if (interfaces.length > 0) {
            return Proxy.newProxyInstance(
                targetClass.getClassLoader(),
                interfaces,
                new AopInvocationHandler(target, aspects)
            );
        } else {
            System.out.println("⚠️ AOP: Cannot create proxy for class without interfaces: " + targetClass.getName());
            return target;
        }
    }
    
    public boolean needsProxy(Object target) {
        if (aspects.isEmpty()) {
            System.out.println("🔍 AOP: No aspects available, proxy not needed for " + target.getClass().getSimpleName());
            return false;
        }
        
        Class<?> targetClass = target.getClass();
        Method[] methods = targetClass.getDeclaredMethods();
        
        System.out.println("🔍 AOP: Checking if proxy needed for " + targetClass.getSimpleName() + " with " + methods.length + " methods");
        
        for (AspectMetadata aspect : aspects) {
            System.out.println("  🔍 Checking aspect: " + aspect.getAspectClass().getSimpleName());
            for (Method method : methods) {
                List<AdviceMetadata> matchingAdvice = aspect.getMatchingAdvice(method, targetClass);
                if (!matchingAdvice.isEmpty()) {
                    System.out.println("  ✅ Found " + matchingAdvice.size() + " matching advice for method: " + method.getName());
                    return true;
                } else {
                    System.out.println("  ❌ No matching advice for method: " + method.getName());
                }
            }
        }
        
        return false;
    }
    
    private static class AopInvocationHandler implements InvocationHandler {
        
        private final Object target;
        private final List<AspectMetadata> aspects;
        
        public AopInvocationHandler(Object target, List<AspectMetadata> aspects) {
            this.target = target;
            this.aspects = aspects;
        }
        
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getDeclaringClass() == Object.class) {
                return method.invoke(target, args);
            }
            
            List<AdviceMetadata> allMatchingAdvice = new ArrayList<>();
            for (AspectMetadata aspect : aspects) {
                allMatchingAdvice.addAll(aspect.getMatchingAdvice(method, target.getClass()));
            }
            
            if (allMatchingAdvice.isEmpty()) {
                return method.invoke(target, args);
            }
            
            return applyAdvice(method, args, target, proxy, allMatchingAdvice);
        }
        
        private Object applyAdvice(Method method, Object[] args, Object target, Object proxy,
                                 List<AdviceMetadata> allAdvice) throws Throwable {
            
            List<AdviceMetadata> aroundAdvice = new ArrayList<>();
            List<AdviceMetadata> beforeAdvice = new ArrayList<>();
            List<AdviceMetadata> afterAdvice = new ArrayList<>();
            List<AdviceMetadata> afterReturningAdvice = new ArrayList<>();
            List<AdviceMetadata> afterThrowingAdvice = new ArrayList<>();
            
            for (AdviceMetadata advice : allAdvice) {
                switch (advice.getType()) {
                    case AROUND:
                        aroundAdvice.add(advice);
                        break;
                    case BEFORE:
                        beforeAdvice.add(advice);
                        break;
                    case AFTER:
                        afterAdvice.add(advice);
                        break;
                    case AFTER_RETURNING:
                        afterReturningAdvice.add(advice);
                        break;
                    case AFTER_THROWING:
                        afterThrowingAdvice.add(advice);
                        break;
                }
            }
            
            Object result = null;
            Throwable exception = null;
            
            try {
                JoinPoint joinPoint = new MethodJoinPoint(method, args, target, proxy);
                for (AdviceMetadata advice : beforeAdvice) {
                    try {
                        advice.invoke(joinPoint);
                    } catch (Exception e) {
                        System.err.println("❌ Error in @Before advice: " + e.getMessage());
                    }
                }
                
                if (!aroundAdvice.isEmpty()) {
                    ProceedingJoinPoint pjp = new MethodProceedingJoinPoint(
                            method, args, target, proxy, aroundAdvice, 0);
                    result = pjp.proceed();
                } else {
                    result = method.invoke(target, args);
                }
                
                for (AdviceMetadata advice : afterReturningAdvice) {
                    try {
                        advice.invoke(joinPoint, result);
                    } catch (Exception e) {
                        System.err.println("❌ Error in @AfterReturning advice: " + e.getMessage());
                    }
                }
                
            } catch (Throwable t) {
                exception = t;
                
                JoinPoint joinPoint = new MethodJoinPoint(method, args, target, proxy);
                for (AdviceMetadata advice : afterThrowingAdvice) {
                    try {
                        advice.invoke(joinPoint, exception);
                    } catch (Exception e) {
                        System.err.println("❌ Error in @AfterThrowing advice: " + e.getMessage());
                    }
                }
                
                throw exception;
            } finally {
                JoinPoint joinPoint = new MethodJoinPoint(method, args, target, proxy);
                for (AdviceMetadata advice : afterAdvice) {
                    try {
                        advice.invoke(joinPoint);
                    } catch (Exception e) {
                        System.err.println("❌ Error in @After advice: " + e.getMessage());
                    }
                }
            }
            
            return result;
        }
    }
} 