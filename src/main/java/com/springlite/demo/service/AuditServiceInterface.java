package com.springlite.demo.service;

/**
 * 📝 감사 로그 서비스 인터페이스
 * REQUIRES_NEW 전파 속성 테스트를 위한 인터페이스
 */
public interface AuditServiceInterface {
    
    /**
     * 📝 감사 로그 기록
     */
    void logAudit(String action, String details);
    
    /**
     * 📋 모든 감사 로그 조회
     */
    void printAllAuditLogs();
} 