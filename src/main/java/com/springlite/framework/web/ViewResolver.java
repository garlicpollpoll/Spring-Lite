package com.springlite.framework.web;

public interface ViewResolver {
    
    /**
     * 뷰 이름을 실제 View 객체로 해결합니다.
     * 
     * @param viewName 뷰 이름 (예: "user/list")
     * @return 해결된 View 객체
     * @throws Exception 뷰 해결 중 발생할 수 있는 예외
     */
    View resolveViewName(String viewName) throws Exception;
} 