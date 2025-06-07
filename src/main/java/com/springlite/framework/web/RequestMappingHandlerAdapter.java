package com.springlite.framework.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springlite.framework.annotations.PathVariable;
import com.springlite.framework.annotations.RequestBody;
import com.springlite.framework.annotations.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @RequestMapping, @GetMapping, @PostMapping 어노테이션 기반
 * Controller 메소드를 실행하는 HandlerAdapter
 */
public class RequestMappingHandlerAdapter implements HandlerAdapter {
    
    private ObjectMapper objectMapper = new ObjectMapper();
    
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
        
        // 🔥 확장된 매개변수 해결: @PathVariable, @RequestParam, @RequestBody 지원
        Object[] methodArgs = resolveMethodArguments(controllerMethod, request, response, handlerMapping);
        
        // Controller 메소드 실행
        return controllerMethod.invoke(controllerInstance, methodArgs);
    }
    
    /**
     * 🔥 핵심 확장: 메소드 매개변수를 해결하는 메소드
     * @PathVariable, @RequestParam, @RequestBody 등을 처리
     */
    private Object[] resolveMethodArguments(Method method, HttpServletRequest request, 
                                          HttpServletResponse response, HandlerMapping handlerMapping) throws Exception {
        
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];
        
        // URL 패턴에서 경로 변수 추출
        Map<String, String> pathVariables = extractPathVariables(request, handlerMapping);
        
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Class<?> paramType = parameter.getType();
            
            // @PathVariable 처리
            if (parameter.isAnnotationPresent(PathVariable.class)) {
                args[i] = resolvePathVariable(parameter, pathVariables);
            }
            // @RequestParam 처리
            else if (parameter.isAnnotationPresent(RequestParam.class)) {
                args[i] = resolveRequestParam(parameter, request);
            }
            // @RequestBody 처리
            else if (parameter.isAnnotationPresent(RequestBody.class)) {
                args[i] = resolveRequestBody(parameter, request);
            }
            // 기본 Servlet API 타입 처리
            else if (paramType == HttpServletRequest.class) {
                args[i] = request;
            } else if (paramType == HttpServletResponse.class) {
                args[i] = response;
            } else {
                // 지원하지 않는 매개변수 타입
                throw new IllegalArgumentException("Unsupported parameter type: " + paramType.getName() + 
                                                 " (parameter: " + parameter.getName() + ")");
            }
        }
        
        return args;
    }
    
    /**
     * URL 패턴에서 경로 변수를 추출
     * 예: /users/{id} + /users/123 → {id: "123"}
     */
    private Map<String, String> extractPathVariables(HttpServletRequest request, HandlerMapping handlerMapping) {
        Map<String, String> pathVariables = new HashMap<>();
        
        // 🔥 핵심 수정: 실제 Spring Framework 방식 적용
        // request.getRequestURI()를 사용하여 전체 요청 경로 가져오기
        String requestPath = request.getRequestURI();
        
        // 매핑된 패턴
        String pattern = getUrlPattern(handlerMapping);
        
        System.out.println("🔍 Path Variables Debug:");
        System.out.println("  Request Path: " + requestPath);
        System.out.println("  URL Pattern: " + pattern);
        
        if (pattern != null && pattern.contains("{")) {
            // {id}, {name} 같은 패턴 변수를 찾아서 실제 값과 매칭
            // 패턴을 정규식으로 변환: /users/{id} → /users/([^/]+)
            String regexPattern = pattern.replaceAll("\\{([^}]+)\\}", "([^/]+)");
            Pattern regex = Pattern.compile(regexPattern);
            Matcher matcher = regex.matcher(requestPath);
            
            System.out.println("  Regex Pattern: " + regexPattern);
            System.out.println("  Matches: " + matcher.matches());
            
            if (matcher.matches()) {
                // 패턴에서 변수명 추출
                Pattern varPattern = Pattern.compile("\\{([^}]+)\\}");
                Matcher varMatcher = varPattern.matcher(pattern);
                
                int groupIndex = 1;
                while (varMatcher.find()) {
                    String varName = varMatcher.group(1);
                    String varValue = matcher.group(groupIndex++);
                    pathVariables.put(varName, varValue);
                    System.out.println("  Variable: " + varName + " = " + varValue);
                }
            }
        }
        
        return pathVariables;
    }
    
    /**
     * HandlerMapping에서 URL 패턴을 가져오는 메소드
     * 실제로는 HandlerMapping에 이 정보가 저장되어야 함
     */
    private String getUrlPattern(HandlerMapping handlerMapping) {
        // 🔥 실제 구현: HandlerMapping에서 URL 패턴 가져오기
        return handlerMapping.getUrlPattern();
    }
    
    /**
     * @PathVariable 어노테이션이 있는 매개변수를 해결
     */
    private Object resolvePathVariable(Parameter parameter, Map<String, String> pathVariables) {
        PathVariable annotation = parameter.getAnnotation(PathVariable.class);
        String varName = annotation.value();
        
        // 어노테이션에 값이 없으면 매개변수 이름 사용
        if (varName.isEmpty()) {
            varName = parameter.getName();
        }
        
        String value = pathVariables.get(varName);
        
        if (value == null && annotation.required()) {
            throw new IllegalArgumentException("Required path variable '" + varName + "' is not present");
        }
        
        // 타입 변환
        return convertValue(value, parameter.getType());
    }
    
    /**
     * @RequestParam 어노테이션이 있는 매개변수를 해결
     */
    private Object resolveRequestParam(Parameter parameter, HttpServletRequest request) {
        RequestParam annotation = parameter.getAnnotation(RequestParam.class);
        String paramName = annotation.value();
        
        // 어노테이션에 값이 없으면 매개변수 이름 사용
        if (paramName.isEmpty()) {
            paramName = parameter.getName();
        }
        
        String value = request.getParameter(paramName);
        
        // 기본값 처리
        if (value == null && !annotation.defaultValue().isEmpty()) {
            value = annotation.defaultValue();
        }
        
        if (value == null && annotation.required()) {
            throw new IllegalArgumentException("Required request parameter '" + paramName + "' is not present");
        }
        
        // 타입 변환
        return convertValue(value, parameter.getType());
    }
    
    /**
     * @RequestBody 어노테이션이 있는 매개변수를 해결
     */
    private Object resolveRequestBody(Parameter parameter, HttpServletRequest request) throws Exception {
        // HTTP 요청 본문 읽기
        StringBuilder body = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
        }
        
        String json = body.toString();
        if (json.isEmpty()) {
            RequestBody annotation = parameter.getAnnotation(RequestBody.class);
            if (annotation.required()) {
                throw new IllegalArgumentException("Required request body is missing");
            }
            return null;
        }
        
        // JSON을 객체로 변환
        return objectMapper.readValue(json, parameter.getType());
    }
    
    /**
     * 문자열 값을 특정 타입으로 변환
     */
    private Object convertValue(String value, Class<?> targetType) {
        if (value == null) {
            return null;
        }
        
        if (targetType == String.class) {
            return value;
        } else if (targetType == Long.class || targetType == long.class) {
            return Long.parseLong(value);
        } else if (targetType == Integer.class || targetType == int.class) {
            return Integer.parseInt(value);
        } else if (targetType == Boolean.class || targetType == boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (targetType == Double.class || targetType == double.class) {
            return Double.parseDouble(value);
        } else {
            throw new IllegalArgumentException("Unsupported parameter type for conversion: " + targetType.getName());
        }
    }
} 