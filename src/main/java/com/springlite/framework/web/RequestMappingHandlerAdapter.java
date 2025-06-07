package com.springlite.framework.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * @RequestMapping, @GetMapping, @PostMapping 어노테이션 기반
 * Controller 메소드를 실행하는 HandlerAdapter
 */
public class RequestMappingHandlerAdapter implements HandlerAdapter {
    
    @Override
    public boolean supports(Object handler) {
        // HandlerMapping 객체를 지원
        return handler instanceof HandlerMapping;
    }
    
    @Override
    public Object handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        
        HandlerMapping handlerMapping = (HandlerMapping) handler;
        Object controllerInstance = handlerMapping.getHandler();
        Method controllerMethod = handlerMapping.getMethod();
        
        System.out.println("RequestMappingHandlerAdapter: Executing " + 
                          controllerInstance.getClass().getSimpleName() + "." + controllerMethod.getName());
        
        // 현재는 매개변수 없는 메소드만 지원
        // 향후 @PathVariable, @RequestParam 등을 여기서 해결할 수 있음
        Object[] methodArgs = resolveMethodArguments(controllerMethod, request, response);
        
        // Controller 메소드 실행
        return controllerMethod.invoke(controllerInstance, methodArgs);
    }
    
    /**
     * 메소드 매개변수를 해결하는 메소드
     * 현재는 단순하게 구현하지만, 실제 Spring에서는 여기서
     * @PathVariable, @RequestParam, @RequestBody 등을 처리함
     */
    private Object[] resolveMethodArguments(Method method, HttpServletRequest request, 
                                          HttpServletResponse response) {
        
        Class<?>[] parameterTypes = method.getParameterTypes();
        Object[] args = new Object[parameterTypes.length];
        
        // 현재는 매개변수가 없는 메소드만 지원
        // 향후 확장 가능한 구조로 설계
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> paramType = parameterTypes[i];
            
            if (paramType == HttpServletRequest.class) {
                args[i] = request;
            } else if (paramType == HttpServletResponse.class) {
                args[i] = response;
            } else {
                // 지원하지 않는 매개변수 타입
                throw new IllegalArgumentException("Unsupported parameter type: " + paramType.getName());
            }
        }
        
        return args;
    }
    
    /**
     * HandlerMapping 내부 클래스를 외부에서 접근 가능하도록 독립적인 클래스로 분리
     */
    public static class HandlerMapping {
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