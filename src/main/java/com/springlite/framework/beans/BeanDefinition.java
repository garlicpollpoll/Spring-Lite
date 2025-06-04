package com.springlite.framework.beans;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class BeanDefinition {
    private String beanName;
    private Class<?> beanClass;
    private Object beanInstance;
    private boolean singleton = true;
    private boolean isLazy = false;
    private List<Field> autowiredFields = new ArrayList<>();
    private List<Method> autowiredMethods = new ArrayList<>();
    private Constructor<?> autowiredConstructor;
    
    public BeanDefinition(String beanName, Class<?> beanClass) {
        this.beanName = beanName;
        this.beanClass = beanClass;
    }

    public String getBeanName() {
        return beanName;
    }
    
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }
    
    public Class<?> getBeanClass() {
        return beanClass;
    }
    
    public void setBeanClass(Class<?> beanClass) {
        this.beanClass = beanClass;
    }
    
    public Object getBeanInstance() {
        return beanInstance;
    }
    
    public void setBeanInstance(Object beanInstance) {
        this.beanInstance = beanInstance;
    }
    
    public boolean isSingleton() {
        return singleton;
    }
    
    public void setSingleton(boolean singleton) {
        this.singleton = singleton;
    }
    
    public boolean isLazy() {
        return isLazy;
    }
    
    public void setLazy(boolean lazy) {
        isLazy = lazy;
    }
    
    public List<Field> getAutowiredFields() {
        return autowiredFields;
    }
    
    public void setAutowiredFields(List<Field> autowiredFields) {
        this.autowiredFields = autowiredFields;
    }
    
    public List<Method> getAutowiredMethods() {
        return autowiredMethods;
    }
    
    public void setAutowiredMethods(List<Method> autowiredMethods) {
        this.autowiredMethods = autowiredMethods;
    }
    
    public Constructor<?> getAutowiredConstructor() {
        return autowiredConstructor;
    }
    
    public void setAutowiredConstructor(Constructor<?> autowiredConstructor) {
        this.autowiredConstructor = autowiredConstructor;
    }
} 