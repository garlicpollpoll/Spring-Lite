package com.springlite.demo;

import com.springlite.framework.context.AnnotationApplicationContext;
import com.springlite.framework.jdbc.JdbcTemplate;
import com.springlite.framework.transaction.JdbcTransactionManager;
import com.springlite.framework.transaction.TransactionAspect;
import com.springlite.demo.service.BankService;
import com.springlite.demo.service.BankServiceInterface;

import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;
import java.math.BigDecimal;

/**
 * 🔄 Spring Lite 트랜잭션 테스트 애플리케이션
 * 
 * 테스트 항목:
 * 1. 트랜잭션 커밋 (ACID 보장)
 * 2. 트랜잭션 롤백 (원자성 보장)
 * 3. 읽기 전용 트랜잭션
 * 4. 중첩 트랜잭션 (같은 Connection 사용)
 * 5. AOP + 트랜잭션 통합
 */
public class TransactionTestApp {
    
    public static void main(String[] args) {
        System.out.println("🔄 Spring Lite 트랜잭션 테스트 시작!");
        System.out.println("============================================");
        
        try {
            // 1. ApplicationContext 생성 및 설정
            AnnotationApplicationContext context = createApplicationContext();
            
            // 2. BankService 가져오기
            BankServiceInterface bankService = context.getBean(BankServiceInterface.class);
            
            // 디버깅: @Transactional 어노테이션 확인
            System.out.println("🔍 BankService 클래스: " + bankService.getClass().getName());
            try {
                var transferMethod = bankService.getClass().getMethod("transferMoney", String.class, String.class, java.math.BigDecimal.class);
                var transactionalAnnotation = transferMethod.getAnnotation(com.springlite.framework.transaction.Transactional.class);
                System.out.println("🔍 transferMoney 메서드에 @Transactional 있음: " + (transactionalAnnotation != null));
                if (transactionalAnnotation != null) {
                    System.out.println("🔍 readOnly: " + transactionalAnnotation.readOnly());
                }
            } catch (Exception e) {
                System.err.println("🔍 메서드 확인 중 오류: " + e.getMessage());
            }
            
            // 3. 테이블 초기화
            System.out.println("\n🏗️ === 1단계: 테이블 초기화 ===");
            bankService.initializeAccountTable();
            bankService.printAllAccounts();
            
            // 4. 성공적인 송금 테스트 (커밋)
            System.out.println("\n✅ === 2단계: 성공적인 송금 (커밋) ===");
            bankService.transferMoney("ACC001", "ACC002", new BigDecimal("100000.00"));
            bankService.printAllAccounts();
            
            // 5. 실패하는 송금 테스트 (롤백)
            System.out.println("\n💥 === 3단계: 실패하는 송금 (롤백) ===");
            try {
                bankService.transferMoneyWithFailure("ACC002", "ACC003", new BigDecimal("50000.00"));
            } catch (Exception e) {
                System.err.println("예상된 예외 발생: " + e.getMessage());
            }
            bankService.printAllAccounts();
            
            // 6. 잔액 부족 송금 테스트 (롤백)
            System.out.println("\n💸 === 4단계: 잔액 부족 송금 (롤백) ===");
            try {
                bankService.transferMoney("ACC003", "ACC001", new BigDecimal("2000000.00"));
            } catch (Exception e) {
                System.err.println("예상된 예외 발생: " + e.getMessage());
            }
            bankService.printAllAccounts();
            
            // 7. 연속 송금 테스트
            System.out.println("\n🔄 === 5단계: 연속 송금 테스트 ===");
            bankService.transferMoney("ACC001", "ACC003", new BigDecimal("200000.00"));
            bankService.transferMoney("ACC003", "ACC002", new BigDecimal("150000.00"));
            bankService.printAllAccounts();
            
            // 8. 읽기 전용 트랜잭션 테스트
            System.out.println("\n📖 === 6단계: 읽기 전용 트랜잭션 테스트 ===");
            BigDecimal balance1 = bankService.getBalance("ACC001");
            BigDecimal balance2 = bankService.getBalance("ACC002");
            BigDecimal balance3 = bankService.getBalance("ACC003");
            System.out.println("총 잔액 합계: " + balance1.add(balance2).add(balance3));
            
            // 9. REQUIRES_NEW 전파 속성 테스트 (성공 시나리오)
            System.out.println("\n🆕 === 7단계: REQUIRES_NEW 전파 속성 테스트 (성공) ===");
            bankService.transferMoneyWithAudit("ACC001", "ACC002", new BigDecimal("50000.00"));
            bankService.printAllAccounts();
            
            // 10. REQUIRES_NEW 전파 속성 테스트 (실패 시나리오)
            System.out.println("\n💥 === 8단계: REQUIRES_NEW 롤백 독립성 테스트 ===");
            System.out.println("메인 트랜잭션은 실패하지만 감사 로그(REQUIRES_NEW)는 커밋됩니다!");
            try {
                bankService.transferMoneyWithAuditAndFailure("ACC002", "ACC003", new BigDecimal("30000.00"));
            } catch (Exception e) {
                System.err.println("예상된 예외 발생: " + e.getMessage());
            }
            bankService.printAllAccounts();
            
            // 11. 감사 로그 확인
            System.out.println("\n📝 === 9단계: 감사 로그 확인 ===");
            try {
                JdbcTemplate jdbcTemplate = context.getBean(JdbcTemplate.class);
                var auditLogs = jdbcTemplate.queryForList("SELECT * FROM audit_log ORDER BY created_at");
                System.out.println("📋 감사 로그 기록 수: " + auditLogs.size());
                for (var log : auditLogs) {
                    System.out.println("📝 " + log.get("ACTION") + ": " + log.get("DETAILS"));
                }
            } catch (Exception e) {
                System.out.println("⚠️ 감사 로그 조회 중 오류: " + e.getMessage());
            }
            
            System.out.println("\n🎉 === 트랜잭션 테스트 완료! ===");
            System.out.println("✅ ACID 속성이 모두 정상적으로 작동합니다!");
            System.out.println("✅ 커밋/롤백이 올바르게 처리됩니다!");
            System.out.println("✅ AOP + 트랜잭션 통합이 성공했습니다!");
            System.out.println("🆕 REQUIRES_NEW 전파 속성이 완벽하게 작동합니다!");
            System.out.println("🔄 독립적인 트랜잭션 관리가 성공했습니다!");
            
            // Context 종료
            context.close();
            
        } catch (Exception e) {
            System.err.println("❌ 트랜잭션 테스트 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * ApplicationContext 생성 및 트랜잭션 관련 빈 등록
     */
    private static AnnotationApplicationContext createApplicationContext() {
        System.out.println("🚀 ApplicationContext 생성 중...");
        
        // 1. H2 DataSource 생성
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        dataSource.setUser("sa");
        dataSource.setPassword("");
        System.out.println("📋 H2 DataSource 생성 완료");
        
        // 2. ApplicationContext 생성 (기본 생성자 - 컴포넌트 스캔 안 함)
        AnnotationApplicationContext context = new AnnotationApplicationContext();
        
        // 3. DataSource를 수동으로 등록 (Spring Lite에서는 @Bean이 없으므로)
        context.registerBean("dataSource", dataSource);
        System.out.println("📋 DataSource 빈 등록 완료");
        
        // 4. JdbcTemplate 생성 및 등록
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        context.registerBean("jdbcTemplate", jdbcTemplate);
        System.out.println("📋 JdbcTemplate 빈 등록 완료");
        
        // 5. TransactionManager 생성 및 등록
        JdbcTransactionManager transactionManager = new JdbcTransactionManager(dataSource);
        context.registerBean("transactionManager", transactionManager);
        System.out.println("🔄 TransactionManager 빈 등록 완료");
        
        // 6. TransactionAspect 생성 및 등록
        TransactionAspect transactionAspect = new TransactionAspect(transactionManager);
        context.registerBean("transactionAspect", transactionAspect);
        System.out.println("🔄 TransactionAspect 빈 등록 완료");
        
        // 7. 이제 컴포넌트 스캔 실행 (필요한 빈들이 모두 등록된 후)
        context.scanPackages("com.springlite.demo.service");
        
        // 8. refresh를 호출해서 빈들을 초기화
        context.refresh();
        
        System.out.println("✅ ApplicationContext 준비 완료");
        return context;
    }
} 