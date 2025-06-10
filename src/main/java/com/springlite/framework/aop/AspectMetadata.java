package com.springlite.framework.aop;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Holds metadata about an aspect including its pointcuts and advice methods.
 */
public class AspectMetadata {
    
    private final Object aspectInstance;
    private final Class<?> aspectClass;
    private final Map<String, PointcutMatcher> namedPointcuts;
    private final List<AdviceMetadata> adviceList;
    
    public AspectMetadata(Object aspectInstance) {
        this.aspectInstance = aspectInstance;
        this.aspectClass = aspectInstance.getClass();
        this.namedPointcuts = new HashMap<>();
        this.adviceList = new ArrayList<>();
    }
    
    public void addNamedPointcut(String name, PointcutMatcher pointcut) {
        namedPointcuts.put(name, pointcut);
    }
    
    public void addAdvice(AdviceMetadata advice) {
        adviceList.add(advice);
    }
    
    public Object getAspectInstance() {
        return aspectInstance;
    }
    
    public Class<?> getAspectClass() {
        return aspectClass;
    }
    
    public Map<String, PointcutMatcher> getNamedPointcuts() {
        return namedPointcuts;
    }
    
    public List<AdviceMetadata> getAdviceList() {
        return adviceList;
    }
    
    /**
     * Returns all advice that match the given method.
     */
    public List<AdviceMetadata> getMatchingAdvice(Method method, Class<?> targetClass) {
        List<AdviceMetadata> matchingAdvice = new ArrayList<>();
        
        for (AdviceMetadata advice : adviceList) {
            if (advice.matches(method, targetClass)) {
                matchingAdvice.add(advice);
            }
        }
        
        // Sort by advice type order: @Around, @Before, @After, @AfterReturning, @AfterThrowing
        matchingAdvice.sort((a1, a2) -> {
            return Integer.compare(getAdviceOrder(a1.getType()), getAdviceOrder(a2.getType()));
        });
        
        return matchingAdvice;
    }
    
    private int getAdviceOrder(AdviceType type) {
        switch (type) {
            case AROUND: return 1;
            case BEFORE: return 2;
            case AFTER: return 3;
            case AFTER_RETURNING: return 4;
            case AFTER_THROWING: return 5;
            default: return 999;
        }
    }
} 