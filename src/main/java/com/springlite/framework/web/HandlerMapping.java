package com.springlite.framework.web;

import java.lang.reflect.Method;

/**
 * HTTP 요청을 처리할 Handler(Controller 메소드)와 URL 패턴 정보를 담는 클래스
 * Spring MVC의 HandlerMapping 개념을 구현
 */
public class HandlerMapping {
    private final Object handler;      // Controller 인스턴스
    private final Method method;       // Controller 메소드
    private final String urlPattern;   // URL 패턴 (예: /api/users/{id})
    
    public HandlerMapping(Object handler, Method method, String urlPattern) {
        this.handler = handler;
        this.method = method;
        this.urlPattern = urlPattern;
    }
    
    public Object getHandler() {
        return handler;
    }
    
    public Method getMethod() {
        return method;
    }
    
    public String getUrlPattern() {
        return urlPattern;
    }
    
    @Override
    public String toString() {
        return "HandlerMapping{" +
                "handler=" + handler.getClass().getSimpleName() +
                ", method=" + method.getName() +
                ", urlPattern='" + urlPattern + '\'' +
                '}';
    }
} 