package com.springlite.framework.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Spring MVC의 HandlerAdapter 인터페이스
 * 다양한 타입의 Handler를 실행할 수 있는 어댑터 패턴
 */
public interface HandlerAdapter {
    
    /**
     * 이 어댑터가 주어진 핸들러를 지원하는지 확인
     * @param handler 핸들러 객체
     * @return 지원 여부
     */
    boolean supports(Object handler);
    
    /**
     * 핸들러를 실행하고 결과를 반환
     * @param request HTTP 요청
     * @param response HTTP 응답  
     * @param handler 핸들러 객체
     * @return 핸들러 실행 결과
     * @throws Exception 실행 중 예외
     */
    Object handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception;
} 