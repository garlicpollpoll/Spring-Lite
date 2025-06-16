package com.springlite.demo.service;

import com.springlite.framework.annotations.Service;
import com.springlite.framework.annotations.Autowired;
import com.springlite.framework.jdbc.JdbcTemplate;
import com.springlite.framework.transaction.Transactional;
import com.springlite.framework.transaction.Propagation;

/**
 * 📝 감사 로그 서비스
 * REQUIRES_NEW 전파 속성 테스트를 위한 별도 서비스
 */
@Service
public class AuditService implements AuditServiceInterface {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    /**
     * 📝 감사 로그 기록 (REQUIRES_NEW 전파 속성)
     * 항상 새로운 독립적인 트랜잭션에서 실행됩니다.
     * 메인 트랜잭션이 실패하더라도 감사 로그는 기록됩니다.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAudit(String action, String details) {
        System.out.println("📝 === 감사 로그 기록 시작 (REQUIRES_NEW) ===");
        System.out.println("Action: " + action);
        System.out.println("Details: " + details);
        
        // 감사 로그 테이블이 없으면 생성
        try {
            jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS audit_log (" +
                "    id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "    action VARCHAR(100) NOT NULL," +
                "    details VARCHAR(500)," +
                "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );
        } catch (Exception e) {
            System.out.println("⚠️ 감사 테이블 생성 중 오류 (무시): " + e.getMessage());
        }
        
        // 감사 로그 기록
        jdbcTemplate.update(
            "INSERT INTO audit_log (action, details) VALUES (?, ?)",
            action, details
        );
        
        System.out.println("✅ 감사 로그 기록 완료 (REQUIRES_NEW 독립 트랜잭션)");
        System.out.println("================================");
    }
    
    /**
     * 📋 모든 감사 로그 조회
     */
    @Override
    @Transactional(readOnly = true)
    public void printAllAuditLogs() {
        System.out.println("📋 === 감사 로그 전체 조회 ===");
        
        var auditLogs = jdbcTemplate.queryForList("SELECT * FROM audit_log ORDER BY created_at");
        System.out.println("📋 감사 로그 기록 수: " + auditLogs.size());
        
        for (var log : auditLogs) {
            System.out.println("📝 " + log.get("ACTION") + ": " + log.get("DETAILS") + 
                             " (시간: " + log.get("CREATED_AT") + ")");
        }
        
        System.out.println("================================");
    }
} 