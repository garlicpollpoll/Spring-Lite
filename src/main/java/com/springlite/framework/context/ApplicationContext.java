package com.springlite.framework.context;

public interface ApplicationContext extends BeanFactory {
    
    void refresh();
    
    void close();
    
    boolean isRunning();
    
    String[] getBeanDefinitionNames();
} 