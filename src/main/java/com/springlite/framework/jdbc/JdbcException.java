package com.springlite.framework.jdbc;

/**
 * 🚨 JDBC Exception
 * Spring Lite의 JDBC 관련 예외 클래스
 * 
 * SQL 실행 중 발생하는 예외를 래핑하여 더 명확한 에러 정보 제공
 */
public class JdbcException extends RuntimeException {
    
    /**
     * 메시지만으로 예외 생성
     */
    public JdbcException(String message) {
        super(message);
    }
    
    /**
     * 메시지와 원인 예외로 예외 생성
     */
    public JdbcException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * 원인 예외만으로 예외 생성
     */
    public JdbcException(Throwable cause) {
        super(cause);
    }
} 