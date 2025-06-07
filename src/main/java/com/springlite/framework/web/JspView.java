package com.springlite.framework.web;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class JspView implements View {
    
    private String url;
    
    public JspView(String url) {
        this.url = url;
    }
    
    @Override
    public void render(Map<String, Object> model, HttpServletRequest request, 
                      HttpServletResponse response) throws Exception {
        
        System.out.println("🎨 JspView: Rendering view " + url);
        
        // 모델 데이터를 request attribute로 설정
        if (model != null) {
            for (Map.Entry<String, Object> entry : model.entrySet()) {
                request.setAttribute(entry.getKey(), entry.getValue());
                System.out.println("  Model: " + entry.getKey() + " = " + entry.getValue());
            }
        }
        
        // 🔥 핵심 수정: 실제 Spring Framework InternalResourceView 방식 적용
        // Spring은 JSP를 위해 forward()를 사용하되, 특별한 조건에서만 사용
        
        // JSP 서블릿이 처리할 수 있도록 URL을 정규화
        String dispatcherPath = url;
        if (!dispatcherPath.startsWith("/")) {
            dispatcherPath = "/" + dispatcherPath;
        }
        
        System.out.println("  → Forwarding to JSP: " + dispatcherPath);
        
        // 🔥 핵심: Spring의 InternalResourceView는 forward() 사용
        // 하지만 StackOverflow를 피하기 위해 조건부 처리
        RequestDispatcher dispatcher = request.getRequestDispatcher(dispatcherPath);
        if (dispatcher != null) {
            // Spring이 JSP를 처리하는 방식: forward 사용
            // 단, DispatcherServlet에서 이미 호출된 경우가 아닐 때만
            String forwardRequestUri = (String) request.getAttribute("javax.servlet.forward.request_uri");
            
            if (forwardRequestUri == null) {
                // 최초 forward 요청 - 정상적인 Spring 방식
                System.out.println("  → Using forward (Spring standard approach)");
                dispatcher.forward(request, response);
                System.out.println("✅ JSP rendered successfully via forward");
            } else {
                // 이미 forward된 요청 - include 사용으로 무한루프 방지
                System.out.println("  → Using include (avoiding infinite loop)");
                dispatcher.include(request, response);
                System.out.println("✅ JSP rendered successfully via include");
            }
        } else {
            throw new RuntimeException("Could not get RequestDispatcher for [" + dispatcherPath + "]");
        }
    }
    
    @Override
    public String getContentType() {
        return "text/html;charset=UTF-8";
    }
    
    public String getUrl() {
        return url;
    }
    
    @Override
    public String toString() {
        return "JspView{url='" + url + "'}";
    }
} 