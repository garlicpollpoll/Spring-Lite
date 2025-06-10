package com.springlite.framework.aop;

import com.springlite.framework.aop.annotations.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Scans for @Aspect annotated classes and extracts their pointcuts and advice.
 */
public class AspectScanner {
    
    /**
     * Processes an aspect instance and extracts its metadata.
     */
    public static AspectMetadata processAspect(Object aspectInstance) {
        Class<?> aspectClass = aspectInstance.getClass();
        
        if (!aspectClass.isAnnotationPresent(Aspect.class)) {
            throw new IllegalArgumentException("Class " + aspectClass.getName() + " is not annotated with @Aspect");
        }
        
        AspectMetadata aspectMetadata = new AspectMetadata(aspectInstance);
        
        // Process all methods in the aspect
        Method[] methods = aspectClass.getDeclaredMethods();
        
        // First pass: collect @Pointcut definitions
        for (Method method : methods) {
            if (method.isAnnotationPresent(Pointcut.class)) {
                processPointcutMethod(method, aspectMetadata);
            }
        }
        
        // Second pass: collect advice methods
        for (Method method : methods) {
            if (method.isAnnotationPresent(Before.class)) {
                processBeforeAdvice(method, aspectMetadata);
            } else if (method.isAnnotationPresent(After.class)) {
                processAfterAdvice(method, aspectMetadata);
            } else if (method.isAnnotationPresent(Around.class)) {
                processAroundAdvice(method, aspectMetadata);
            } else if (method.isAnnotationPresent(AfterReturning.class)) {
                processAfterReturningAdvice(method, aspectMetadata);
            } else if (method.isAnnotationPresent(AfterThrowing.class)) {
                processAfterThrowingAdvice(method, aspectMetadata);
            }
        }
        
        System.out.println("🔍 AOP: Processed aspect " + aspectClass.getSimpleName() + 
                          " with " + aspectMetadata.getAdviceList().size() + " advice methods");
        
        return aspectMetadata;
    }
    
    /**
     * Processes a @Pointcut annotated method.
     */
    private static void processPointcutMethod(Method method, AspectMetadata aspectMetadata) {
        Pointcut pointcut = method.getAnnotation(Pointcut.class);
        String expression = pointcut.value();
        String pointcutName = method.getName();
        
        PointcutMatcher matcher = new PointcutMatcher(expression);
        aspectMetadata.addNamedPointcut(pointcutName, matcher);
        
        System.out.println("  📍 Found pointcut: " + pointcutName + "() = " + expression);
    }
    
    /**
     * Processes a @Before annotated method.
     */
    private static void processBeforeAdvice(Method method, AspectMetadata aspectMetadata) {
        Before before = method.getAnnotation(Before.class);
        String expression = before.value();
        
        PointcutMatcher matcher = resolvePointcutExpression(expression, aspectMetadata);
        AdviceMetadata advice = new AdviceMetadata(method, aspectMetadata.getAspectInstance(), 
                                                  AdviceType.BEFORE, matcher);
        
        aspectMetadata.addAdvice(advice);
        System.out.println("  ⬅️ Found @Before advice: " + method.getName() + "() with pointcut: " + expression);
    }
    
    /**
     * Processes an @After annotated method.
     */
    private static void processAfterAdvice(Method method, AspectMetadata aspectMetadata) {
        After after = method.getAnnotation(After.class);
        String expression = after.value();
        
        PointcutMatcher matcher = resolvePointcutExpression(expression, aspectMetadata);
        AdviceMetadata advice = new AdviceMetadata(method, aspectMetadata.getAspectInstance(), 
                                                  AdviceType.AFTER, matcher);
        
        aspectMetadata.addAdvice(advice);
        System.out.println("  ➡️ Found @After advice: " + method.getName() + "() with pointcut: " + expression);
    }
    
    /**
     * Processes an @Around annotated method.
     */
    private static void processAroundAdvice(Method method, AspectMetadata aspectMetadata) {
        Around around = method.getAnnotation(Around.class);
        String expression = around.value();
        
        PointcutMatcher matcher = resolvePointcutExpression(expression, aspectMetadata);
        AdviceMetadata advice = new AdviceMetadata(method, aspectMetadata.getAspectInstance(), 
                                                  AdviceType.AROUND, matcher);
        
        aspectMetadata.addAdvice(advice);
        System.out.println("  🔄 Found @Around advice: " + method.getName() + "() with pointcut: " + expression);
    }
    
    /**
     * Processes an @AfterReturning annotated method.
     */
    private static void processAfterReturningAdvice(Method method, AspectMetadata aspectMetadata) {
        AfterReturning afterReturning = method.getAnnotation(AfterReturning.class);
        String expression = !afterReturning.value().isEmpty() ? 
                           afterReturning.value() : afterReturning.pointcut();
        String returning = afterReturning.returning();
        
        PointcutMatcher matcher = resolvePointcutExpression(expression, aspectMetadata);
        AdviceMetadata advice = new AdviceMetadata(method, aspectMetadata.getAspectInstance(), 
                                                  AdviceType.AFTER_RETURNING, matcher, returning, null);
        
        aspectMetadata.addAdvice(advice);
        System.out.println("  ↩️ Found @AfterReturning advice: " + method.getName() + "() with pointcut: " + expression);
    }
    
    /**
     * Processes an @AfterThrowing annotated method.
     */
    private static void processAfterThrowingAdvice(Method method, AspectMetadata aspectMetadata) {
        AfterThrowing afterThrowing = method.getAnnotation(AfterThrowing.class);
        String expression = !afterThrowing.value().isEmpty() ? 
                           afterThrowing.value() : afterThrowing.pointcut();
        String throwing = afterThrowing.throwing();
        
        PointcutMatcher matcher = resolvePointcutExpression(expression, aspectMetadata);
        AdviceMetadata advice = new AdviceMetadata(method, aspectMetadata.getAspectInstance(), 
                                                  AdviceType.AFTER_THROWING, matcher, null, throwing);
        
        aspectMetadata.addAdvice(advice);
        System.out.println("  ❌ Found @AfterThrowing advice: " + method.getName() + "() with pointcut: " + expression);
    }
    
    /**
     * Resolves a pointcut expression, handling both inline expressions and named pointcut references.
     */
    private static PointcutMatcher resolvePointcutExpression(String expression, AspectMetadata aspectMetadata) {
        // Check if it's a named pointcut reference (e.g., "dataAccessOperation()")
        if (expression.contains("(") && expression.endsWith(")") && !expression.contains("execution") 
            && !expression.contains("@annotation") && !expression.contains("within") 
            && !expression.contains("bean") && !expression.contains("*")) {
            
            String pointcutName = expression.substring(0, expression.indexOf("("));
            PointcutMatcher namedPointcut = aspectMetadata.getNamedPointcuts().get(pointcutName);
            
            if (namedPointcut != null) {
                System.out.println("  🔗 명명된 pointcut 참조 해결: " + pointcutName + " → " + namedPointcut.getExpression());
                return namedPointcut;
            } else {
                System.err.println("⚠️ Named pointcut not found: " + pointcutName + ". Available: " + aspectMetadata.getNamedPointcuts().keySet());
                return new PointcutMatcher(""); // Return empty matcher
            }
        }
        
        // It's an inline expression
        return new PointcutMatcher(expression);
    }
} 