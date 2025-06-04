package com.springlite.framework.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springlite.framework.annotations.*;
import com.springlite.framework.context.ApplicationContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class DispatcherServlet extends HttpServlet {
    
    private ApplicationContext applicationContext;
    private Map<String, HandlerMapping> handlerMappings = new HashMap<>();
    private ObjectMapper objectMapper = new ObjectMapper();
    
    public DispatcherServlet(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        initHandlerMappings();
    }
    
    private void initHandlerMappings() {
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        
        for (String beanName : beanNames) {
            Object bean = applicationContext.getBean(beanName);
            Class<?> beanClass = bean.getClass();
            
            if (beanClass.isAnnotationPresent(Controller.class)) {
                String baseMapping = "";
                if (beanClass.isAnnotationPresent(RequestMapping.class)) {
                    RequestMapping requestMapping = beanClass.getAnnotation(RequestMapping.class);
                    baseMapping = requestMapping.value();
                }
                
                Method[] methods = beanClass.getDeclaredMethods();
                for (Method method : methods) {
                    if (method.isAnnotationPresent(RequestMapping.class) ||
                        method.isAnnotationPresent(GetMapping.class) || 
                        method.isAnnotationPresent(PostMapping.class)) {
                        
                        String path = getMethodPath(method);
                        String fullPath = baseMapping + path;
                        RequestMapping.RequestMethod httpMethod = getHttpMethod(method);
                        
                        String key = httpMethod + ":" + fullPath;
                        handlerMappings.put(key, new HandlerMapping(bean, method));
                        
                        System.out.println("Mapped [" + httpMethod + " " + fullPath + "] -> " + 
                                         beanClass.getSimpleName() + "." + method.getName());
                    }
                }
            }
        }
    }
    
    private String getMethodPath(Method method) {
        if (method.isAnnotationPresent(GetMapping.class)) {
            return method.getAnnotation(GetMapping.class).value();
        } else if (method.isAnnotationPresent(PostMapping.class)) {
            return method.getAnnotation(PostMapping.class).value();
        } else if (method.isAnnotationPresent(RequestMapping.class)) {
            return method.getAnnotation(RequestMapping.class).value();
        }
        return "";
    }
    
    private RequestMapping.RequestMethod getHttpMethod(Method method) {
        if (method.isAnnotationPresent(GetMapping.class)) {
            return RequestMapping.RequestMethod.GET;
        } else if (method.isAnnotationPresent(PostMapping.class)) {
            return RequestMapping.RequestMethod.POST;
        } else if (method.isAnnotationPresent(RequestMapping.class)) {
            RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
            RequestMapping.RequestMethod[] methods = requestMapping.method();
            return methods.length > 0 ? methods[0] : RequestMapping.RequestMethod.GET;
        }
        return RequestMapping.RequestMethod.GET;
    }
    
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String method = request.getMethod();
        String path = request.getPathInfo();
        if (path == null) {
            path = request.getServletPath();
        }
        
        String key = method + ":" + path;
        HandlerMapping handlerMapping = handlerMappings.get(key);
        
        if (handlerMapping != null) {
            try {
                Object result = handlerMapping.getMethod().invoke(handlerMapping.getHandler());
                
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                
                if (result != null) {
                    if (result instanceof String) {
                        response.getWriter().write((String) result);
                    } else {
                        String json = objectMapper.writeValueAsString(result);
                        response.getWriter().write(json);
                    }
                } else {
                    response.getWriter().write("{}");
                }
                
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
                e.printStackTrace();
            }
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("{\"error\":\"Handler not found for " + method + " " + path + "\"}");
        }
    }
    
    private static class HandlerMapping {
        private final Object handler;
        private final Method method;
        
        public HandlerMapping(Object handler, Method method) {
            this.handler = handler;
            this.method = method;
        }
        
        public Object getHandler() {
            return handler;
        }
        
        public Method getMethod() {
            return method;
        }
    }
} 