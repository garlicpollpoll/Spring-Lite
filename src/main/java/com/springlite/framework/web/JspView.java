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
        
        // 모델 데이터를 request attribute로 설정
        if (model != null) {
            for (Map.Entry<String, Object> entry : model.entrySet()) {
                request.setAttribute(entry.getKey(), entry.getValue());
            }
        }
        
        // JSP로 포워드
        RequestDispatcher dispatcher = request.getRequestDispatcher(url);
        if (dispatcher != null) {
            dispatcher.forward(request, response);
        } else {
            throw new RuntimeException("Could not get RequestDispatcher for [" + url + "]");
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