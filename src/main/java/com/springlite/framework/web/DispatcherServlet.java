package com.springlite.framework.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springlite.framework.annotations.*;
import com.springlite.framework.context.ApplicationContext;

import javax.servlet.RequestDispatcher;
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
import java.util.concurrent.ConcurrentHashMap;

public class DispatcherServlet extends HttpServlet {
    
    private ApplicationContext applicationContext;
    private Map<String, HandlerMapping> handlerMappings = new HashMap<>();
    private List<HandlerAdapter> handlerAdapters = new ArrayList<>();
    private ObjectMapper objectMapper = new ObjectMapper();
    private ViewResolver viewResolver;
    
    // 핸들러 메서드 캐시
    private Map<String, Object> handlerCache = new ConcurrentHashMap<>();
    
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
                        String httpMethod = getHttpMethod(method);
                        
                        String key = httpMethod + ":" + fullPath;
                        // 🔥 수정: 독립적인 HandlerMapping 클래스 사용
                        handlerMappings.put(key, new HandlerMapping(bean, method, fullPath));
                        
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
    
    private String getHttpMethod(Method method) {
        if (method.isAnnotationPresent(GetMapping.class)) {
            return "GET";
        } else if (method.isAnnotationPresent(PostMapping.class)) {
            return "POST";
        } else if (method.isAnnotationPresent(RequestMapping.class)) {
            RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
            RequestMapping.RequestMethod[] methods = requestMapping.method();
            return methods.length > 0 ? methods[0].name() : "GET";
        }
        return "GET";
    }
    
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String uri = request.getRequestURI();
        String method = request.getMethod();
        
        System.out.println("\n🌐 DispatcherServlet: Processing " + method + " " + uri);
        
        try {
            // 🔥 MVC 요청 처리
            processRequest(request, response);
            
        } catch (Exception e) {
            System.out.println("❌ DispatcherServlet: Error processing request: " + e.getMessage());
            e.printStackTrace();
            
            // 에러 페이지 응답
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().write(
                "<html><body>" +
                "<h2>500 - Internal Server Error</h2>" +
                "<p>Error: " + e.getMessage() + "</p>" +
                "</body></html>"
            );
        }
    }
    
    private void processRequest(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String method = request.getMethod();
        String path = request.getRequestURI();
        
        System.out.println("🔍 Looking for handler: " + method + ":" + path);
        
        // 🔥 핵심 수정: 실제 Spring Framework 방식 적용
        // 1. 정확한 매칭 먼저 시도
        String exactKey = method + ":" + path;
        HandlerMapping handlerMapping = handlerMappings.get(exactKey);
        
        // 2. 정확한 매칭이 없으면 패턴 매칭 시도 (PathVariable 지원)
        if (handlerMapping == null) {
            handlerMapping = findPatternMatch(method, path);
        }
        
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
            // 🔥 핵심 수정: 실제 Spring Framework 방식 적용
            // 핸들러가 없으면 다음 서블릿으로 위임 (JSP 서블릿 또는 DefaultServlet)
            delegateToNextServlet(request, response, path);
        }
    }
    
    /**
     * 🔥 새로 추가: 실제 Spring Framework의 패턴 매칭 로직
     * URL 패턴에서 {id} 같은 PathVariable을 포함한 매칭
     */
    private HandlerMapping findPatternMatch(String httpMethod, String requestPath) {
        System.out.println("🔍 Pattern matching for: " + httpMethod + ":" + requestPath);
        
        for (Map.Entry<String, HandlerMapping> entry : handlerMappings.entrySet()) {
            String key = entry.getKey();
            HandlerMapping mapping = entry.getValue();
            
            // HTTP 메서드 확인
            if (!key.startsWith(httpMethod + ":")) {
                continue;
            }
            
            String urlPattern = mapping.getUrlPattern();
            System.out.println("  Checking pattern: " + urlPattern + " against path: " + requestPath);
            
            if (isPatternMatch(urlPattern, requestPath)) {
                System.out.println("  ✅ Pattern matched!");
                return mapping;
            }
        }
        
        System.out.println("  ❌ No pattern match found");
        return null;
    }
    
    /**
     * 🔥 핵심: 실제 Spring Framework와 동일한 패턴 매칭 알고리즘
     * /users/{id} 와 /users/123 을 매칭
     */
    private boolean isPatternMatch(String pattern, String path) {
        // 정확한 매칭이면 바로 true
        if (pattern.equals(path)) {
            return true;
        }
        
        // PathVariable이 없으면 정확한 매칭만 가능
        if (!pattern.contains("{")) {
            return false;
        }
        
        // 패턴을 정규식으로 변환
        // /users/{id} → /users/([^/]+)
        String regexPattern = pattern.replaceAll("\\{[^}]+\\}", "([^/]+)");
        
        System.out.println("    Regex pattern: " + regexPattern);
        
        return path.matches(regexPattern);
    }
    
    /**
     * 🔥 실제 Spring Framework 핵심 로직: 핸들러가 없으면 다음 서블릿으로 위임
     */
    private void delegateToNextServlet(HttpServletRequest request, HttpServletResponse response, String path) 
            throws ServletException, IOException {
        
        System.out.println("🔄 DispatcherServlet: No handler found, delegating to appropriate servlet for: " + path);
        
        // 🔥 핵심 수정: 실제 Spring Framework 방식 적용
        // JSP 요청 처리 시 무한 루프 방지를 위한 더 정교한 처리
        
        // JSP 요청인지 확인 (실제 .jsp 파일 또는 JSP 내부 리소스)
        if (path.endsWith(".jsp") || path.contains("/WEB-INF/views/")) {
            System.out.println("  → Detected JSP request: " + path);
            
            // forward attribute가 이미 설정되어 있는지 확인 (무한루프 방지)
            String forwardRequestUri = (String) request.getAttribute("javax.servlet.forward.request_uri");
            
            if (forwardRequestUri != null) {
                // 이미 forward된 요청은 404 처리 (무한루프 방지)
                System.out.println("  → Already forwarded request, returning 404 to prevent infinite loop");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            // 🔥 JSP 서블릿으로 위임 - Named Dispatcher 사용
            RequestDispatcher jspDispatcher = getServletContext().getNamedDispatcher("jsp");
            if (jspDispatcher != null) {
                System.out.println("  → Delegating to named JSP servlet");
                // Spring의 방식: JSP는 forward 사용
                jspDispatcher.forward(request, response);
                return;
            }
            
            // Named JSP servlet이 없으면 직접 처리
            System.out.println("  → No named JSP servlet, trying direct dispatch");
            RequestDispatcher dispatcher = request.getRequestDispatcher(path);
            if (dispatcher != null) {
                dispatcher.forward(request, response);
                return;
            }
        }
        
        // 정적 리소스 또는 기타 요청 - Default 서블릿으로 위임
        System.out.println("  → Delegating to default servlet for resource: " + path);
        RequestDispatcher dispatcher = getServletContext().getNamedDispatcher("default");
        if (dispatcher != null) {
            // 🔥 정적 리소스는 include 방식 사용 (Spring Framework 방식)
            dispatcher.include(request, response);
        } else {
            // Default 서블릿도 없으면 404
            System.out.println("❌ No default servlet available, returning 404");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().write(
                "<html><body>" +
                "<h1>404 - Not Found</h1>" +
                "<p>No handler found for: " + request.getMethod() + " " + path + "</p>" +
                "</body></html>"
            );
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
        
        System.out.println("🎬 DispatcherServlet: Handling ModelAndView");
        System.out.println("  ViewName: " + viewName);
        System.out.println("  Model: " + model);
        
        if (viewName != null) {
            // ViewResolver를 통해 View 해결
            View view = viewResolver.resolveViewName(viewName);
            System.out.println("  Resolved View: " + view);
            
            if (view != null) {
                // 콘텐트 타입 설정
                response.setContentType(view.getContentType());
                // View 렌더링
                view.render(model, request, response);
                System.out.println("✅ Rendered view: " + viewName + " with model: " + model);
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
    
    // 🔥 HTTP 메서드별 메서드들을 오버라이드하여 모든 요청을 processRequest()로 위임
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        processRequest(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        processRequest(request, response);
    }
    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        processRequest(request, response);
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        processRequest(request, response);
    }
} 