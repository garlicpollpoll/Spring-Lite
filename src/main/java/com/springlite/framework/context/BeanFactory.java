package com.springlite.framework.context;

public interface BeanFactory {
    
    Object getBean(String name);
    
    <T> T getBean(String name, Class<T> requiredType);
    
    <T> T getBean(Class<T> requiredType);
    
    boolean containsBean(String name);
    
    boolean isSingleton(String name);
    
    Class<?> getType(String name);
} 