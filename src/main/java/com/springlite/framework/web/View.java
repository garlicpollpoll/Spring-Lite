package com.springlite.framework.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public interface View {
    
    /**
     * 뷰를 렌더링합니다.
     * 
     * @param model 뷰에 전달할 모델 데이터
     * @param request HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @throws Exception 렌더링 중 발생할 수 있는 예외
     */
    void render(Map<String, Object> model, HttpServletRequest request, 
                HttpServletResponse response) throws Exception;
    
    /**
     * 뷰의 콘텐트 타입을 반환합니다.
     * 
     * @return 콘텐트 타입 (예: "text/html", "application/json")
     */
    String getContentType();
} 