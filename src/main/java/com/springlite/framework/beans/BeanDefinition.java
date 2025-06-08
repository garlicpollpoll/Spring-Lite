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
    
    // 🔥 새로 추가: @Bean 메서드 지원을 위한 필드들
    private Method beanMethod; // @Bean 어노테이션이 붙은 메서드
    private Object configurationInstance; // @Configuration 클래스의 인스턴스
    private boolean isBeanMethod = false; // @Bean 메서드로 생성된 빈인지 여부
    
    // 🔥 새로 추가: 빈 라이프사이클 지원을 위한 필드들
    private List<Method> postConstructMethods = new ArrayList<>(); // @PostConstruct 메서드들
    private List<Method> preDestroyMethods = new ArrayList<>(); // @PreDestroy 메서드들
    private String initMethodName; // @Bean의 initMethod 속성
    private String destroyMethodName; // @Bean의 destroyMethod 속성
    private boolean defaultCandidate = true; // @Bean의 defaultCandidate 속성
    
    // 기존 생성자 (컴포넌트 스캔용)
    public BeanDefinition(String beanName, Class<?> beanClass) {
        this.beanName = beanName;
        this.beanClass = beanClass;
        this.isBeanMethod = false;
    }
    
    // 🔥 새로 추가: @Bean 메서드용 생성자
    public BeanDefinition(String beanName, Class<?> beanClass, Method beanMethod, Object configurationInstance) {
        this.beanName = beanName;
        this.beanClass = beanClass;
        this.beanMethod = beanMethod;
        this.configurationInstance = configurationInstance;
        this.isBeanMethod = true;
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
    
    // 🔥 새로 추가: @Bean 메서드 관련 getter/setter
    public Method getBeanMethod() {
        return beanMethod;
    }
    
    public void setBeanMethod(Method beanMethod) {
        this.beanMethod = beanMethod;
    }
    
    public Object getConfigurationInstance() {
        return configurationInstance;
    }
    
    public void setConfigurationInstance(Object configurationInstance) {
        this.configurationInstance = configurationInstance;
    }
    
    public boolean isBeanMethod() {
        return isBeanMethod;
    }
    
    public void setBeanMethod(boolean beanMethod) {
        isBeanMethod = beanMethod;
    }
    
    // 🔥 새로 추가: 빈 라이프사이클 관련 getter/setter
    public List<Method> getPostConstructMethods() {
        return postConstructMethods;
    }
    
    public void setPostConstructMethods(List<Method> postConstructMethods) {
        this.postConstructMethods = postConstructMethods;
    }
    
    public List<Method> getPreDestroyMethods() {
        return preDestroyMethods;
    }
    
    public void setPreDestroyMethods(List<Method> preDestroyMethods) {
        this.preDestroyMethods = preDestroyMethods;
    }
    
    public String getInitMethodName() {
        return initMethodName;
    }
    
    public void setInitMethodName(String initMethodName) {
        this.initMethodName = initMethodName;
    }
    
    public String getDestroyMethodName() {
        return destroyMethodName;
    }
    
    public void setDestroyMethodName(String destroyMethodName) {
        this.destroyMethodName = destroyMethodName;
    }
    
    public boolean isDefaultCandidate() {
        return defaultCandidate;
    }
    
    public void setDefaultCandidate(boolean defaultCandidate) {
        this.defaultCandidate = defaultCandidate;
    }
} 