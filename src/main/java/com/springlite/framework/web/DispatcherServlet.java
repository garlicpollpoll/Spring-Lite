package com.springlite.framework.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springlite.framework.annotations.*;
import com.springlite.framework.context.ApplicationContext;
import com.springlite.framework.web.RequestMappingHandlerAdapter.HandlerMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DispatcherServlet extends HttpServlet {
    
    private ApplicationContext applicationContext;
    private Map<String, HandlerMapping> handlerMappings = new HashMap<>();
    private List<HandlerAdapter> handlerAdapters = new ArrayList<>();
    private ObjectMapper objectMapper = new ObjectMapper();
    private ViewResolver viewResolver;
    
    public DispatcherServlet(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        // 기본 ViewResolver 설정 (JSP용)
        this.viewResolver = new InternalResourceViewResolver("/WEB-INF/views/", ".jsp");
        initHandlerAdapters();
        initHandlerMappings();
    }
    
    public DispatcherServlet(ApplicationContext applicationContext, ViewResolver viewResolver) {
        this.applicationContext = applicationContext;
        this.viewResolver = viewResolver;
        initHandlerAdapters();
        initHandlerMappings();
    }
    
    /**
     * 🔥 새로 추가: HandlerAdapter들을 초기화
     */
    private void initHandlerAdapters() {
        // @RequestMapping 기반 Controller를 위한 어댑터 등록
        handlerAdapters.add(new RequestMappingHandlerAdapter());
        
        System.out.println("Initialized HandlerAdapters: " + handlerAdapters.size());
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
                // 🔥 핵심 변경: HandlerAdapter를 통해 실행
                HandlerAdapter adapter = getHandlerAdapter(handlerMapping);
                if (adapter != null) {
                    Object result = adapter.handle(request, response, handlerMapping);
                    
                    // ModelAndView 처리
                    if (result instanceof ModelAndView) {
                        handleModelAndView((ModelAndView) result, request, response);
                    } 
                    // REST API 응답 처리 (기존 로직)
                    else {
                        handleRestResponse(result, response);
                    }
                } else {
                    throw new RuntimeException("No adapter for handler: " + handlerMapping);
                }
                
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
                e.printStackTrace();
            }
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Handler not found for " + method + " " + path + "\"}");
        }
    }
    
    /**
     * 🔥 새로 추가: 주어진 핸들러를 처리할 수 있는 HandlerAdapter 찾기
     */
    private HandlerAdapter getHandlerAdapter(Object handler) {
        for (HandlerAdapter adapter : handlerAdapters) {
            if (adapter.supports(handler)) {
                return adapter;
            }
        }
        return null;
    }
    
    private void handleModelAndView(ModelAndView modelAndView, HttpServletRequest request, 
                                   HttpServletResponse response) throws Exception {
        
        String viewName = modelAndView.getViewName();
        Map<String, Object> model = modelAndView.getModel();
        
        if (viewName != null) {
            // ViewResolver를 통해 View 해결
            View view = viewResolver.resolveViewName(viewName);
            if (view != null) {
                // 콘텐트 타입 설정
                response.setContentType(view.getContentType());
                // View 렌더링
                view.render(model, request, response);
                System.out.println("Rendered view: " + viewName + " with model: " + model);
            } else {
                throw new RuntimeException("Could not resolve view with name '" + viewName + "'");
            }
        } else {
            // 뷰 이름이 없으면 빈 응답
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().write("");
        }
    }
    
    private void handleRestResponse(Object result, HttpServletResponse response) throws IOException {
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
    }
} 