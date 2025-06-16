package com.springlite.demo.service;

import com.springlite.framework.annotations.Service;
import com.springlite.framework.annotations.Autowired;
import com.springlite.framework.jdbc.JdbcTemplate;
import com.springlite.framework.transaction.Transactional;
import com.springlite.framework.transaction.Propagation;

import java.math.BigDecimal;

/**
 * 🏦 Bank Service Implementation
 * 트랜잭션 테스트를 위한 은행 서비스
 */
@Service
public class BankService implements BankServiceInterface {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private AuditServiceInterface auditService;
    
    /**
     * 🏗️ 계좌 테이블 초기화
     */
    public void initializeAccountTable() {
        System.out.println("🏦 계좌 테이블 초기화 중...");
        
        // 기존 테이블 삭제
        jdbcTemplate.execute("DROP TABLE IF EXISTS accounts");
        
        // 새로운 테이블 생성
        jdbcTemplate.execute(
            "CREATE TABLE accounts (" +
            "    account_number VARCHAR(20) PRIMARY KEY," +
            "    account_name VARCHAR(100) NOT NULL," +
            "    balance DECIMAL(15,2) NOT NULL DEFAULT 0.00," +
            "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")"
        );
        
        // 샘플 데이터 생성
        createAccount("ACC001", "김춘식", new BigDecimal("1000000.00"));
        createAccount("ACC002", "박영수", new BigDecimal("500000.00"));
        createAccount("ACC003", "이미영", new BigDecimal("750000.00"));
        
        System.out.println("✅ 계좌 테이블 초기화 완료");
    }
    
    /**
     * 💰 계좌 생성
     */
    @Transactional
    public void createAccount(String accountNumber, String accountName, BigDecimal initialBalance) {
        System.out.println("💰 계좌 생성: " + accountNumber + " (" + accountName + ") - 초기잔액: " + initialBalance);
        
        jdbcTemplate.update(
            "INSERT INTO accounts (account_number, account_name, balance) VALUES (?, ?, ?)",
            accountNumber, accountName, initialBalance
        );
        
        System.out.println("✅ 계좌 생성 완료: " + accountNumber);
    }
    
    /**
     * 💵 잔액 조회 (읽기 전용 트랜잭션)
     */
    @Transactional(readOnly = true)
    public BigDecimal getBalance(String accountNumber) {
        System.out.println("💵 잔액 조회: " + accountNumber);
        
        BigDecimal balance = jdbcTemplate.queryForObject(
            "SELECT balance FROM accounts WHERE account_number = ?",
            BigDecimal.class,
            accountNumber
        );
        
        System.out.println("💵 잔액: " + accountNumber + " = " + balance);
        return balance;
    }
    
    /**
     * 💸 송금 (트랜잭션)
     */
    @Transactional
    public void transferMoney(String fromAccount, String toAccount, BigDecimal amount) {
        System.out.println("🔄 송금 시작: " + fromAccount + " → " + toAccount + " (금액: " + amount + ")");
        
        // 1. 잔액 확인
        BigDecimal fromBalance = getBalance(fromAccount);
        BigDecimal toBalance = getBalance(toAccount);
        
        // 2. 잔액 부족 확인
        if (fromBalance.compareTo(amount) < 0) {
            throw new RuntimeException("잔액이 부족합니다. 현재 잔액: " + fromBalance + ", 송금 금액: " + amount);
        }
        
        // 3. 출금
        jdbcTemplate.update(
            "UPDATE accounts SET balance = balance - ? WHERE account_number = ?",
            amount, fromAccount
        );
        System.out.println("💸 출금 완료: " + fromAccount + " (금액: " + amount + ")");
        
        // 4. 입금
        jdbcTemplate.update(
            "UPDATE accounts SET balance = balance + ? WHERE account_number = ?",
            amount, toAccount
        );
        System.out.println("💰 입금 완료: " + toAccount + " (금액: " + amount + ")");
        
        System.out.println("✅ 송금 완료: " + fromAccount + " → " + toAccount + " (금액: " + amount + ")");
    }
    
    /**
     * 💥 송금 실패 시나리오 (롤백 테스트)
     */
    @Transactional
    public void transferMoneyWithFailure(String fromAccount, String toAccount, BigDecimal amount) {
        System.out.println("💥 송금 실패 시나리오: " + fromAccount + " → " + toAccount + " (금액: " + amount + ")");
        
        // 출금만 실행하고 예외 발생 (롤백 테스트)
        jdbcTemplate.update(
            "UPDATE accounts SET balance = balance - ? WHERE account_number = ?",
            amount, fromAccount
        );
        System.out.println("💸 출금 완료: " + fromAccount + " (금액: " + amount + ")");
        
        // 의도적 예외 발생
        throw new RuntimeException("송금 처리 중 시스템 오류가 발생했습니다! (롤백 테스트)");
    }
    
    /**
     * 📊 모든 계좌 정보 조회
     */
    @Transactional(readOnly = true)
    public void printAllAccounts() {
        System.out.println("📊 === 전체 계좌 현황 ===");
        
        var accounts = jdbcTemplate.queryForList("SELECT * FROM accounts ORDER BY account_number");
        
        for (var account : accounts) {
            // H2 데이터베이스는 컬럼명을 대문자로 저장하므로 대문자 키를 사용
            System.out.println("계좌: " + account.get("ACCOUNT_NUMBER") + 
                             " | 이름: " + account.get("ACCOUNT_NAME") + 
                             " | 잔액: " + account.get("BALANCE"));
        }
        
        System.out.println("================================");
    }
    
    /**
     * 🔄 REQUIRES_NEW 테스트 송금 메서드
     * 메인 송금 로직에서 감사 로그(REQUIRES_NEW)를 호출하는 시나리오
     */
    @Transactional
    public void transferMoneyWithAudit(String fromAccount, String toAccount, BigDecimal amount) {
        System.out.println("🔄 감사 로그와 함께 송금 시작: " + fromAccount + " → " + toAccount + " (금액: " + amount + ")");
        
        // 1. 감사 로그 기록 (REQUIRES_NEW - 독립 트랜잭션)
        auditService.logAudit("TRANSFER_START", "From: " + fromAccount + ", To: " + toAccount + ", Amount: " + amount);
        
        // 2. 송금 실행
        transferMoney(fromAccount, toAccount, amount);
        
        // 3. 또 다른 감사 로그 (REQUIRES_NEW)
        auditService.logAudit("TRANSFER_SUCCESS", "Transfer completed successfully");
        
        System.out.println("✅ 감사 로그와 함께 송금 완료");
    }
    
    /**
     * 💥 REQUIRES_NEW 롤백 테스트
     * 메인 트랜잭션은 실패하지만 감사 로그는 유지되는 시나리오
     */
    @Transactional
    public void transferMoneyWithAuditAndFailure(String fromAccount, String toAccount, BigDecimal amount) {
        System.out.println("💥 감사 로그와 함께 송금 실패 시나리오: " + fromAccount + " → " + toAccount + " (금액: " + amount + ")");
        
        // 1. 감사 로그 기록 (REQUIRES_NEW - 독립 트랜잭션)
        auditService.logAudit("TRANSFER_ATTEMPT", "From: " + fromAccount + ", To: " + toAccount + ", Amount: " + amount);
        
        // 2. 송금 시도 (출금만 실행)
        jdbcTemplate.update(
            "UPDATE accounts SET balance = balance - ? WHERE account_number = ?",
            amount, fromAccount
        );
        System.out.println("💸 출금 완료: " + fromAccount + " (금액: " + amount + ")");
        
        // 3. 실패 감사 로그 (REQUIRES_NEW - 독립 트랜잭션)
        auditService.logAudit("TRANSFER_FAILED", "Transfer failed due to system error");
        
        // 4. 의도적 실패 → 메인 트랜잭션 롤백 (하지만 감사 로그는 유지됨)
        throw new RuntimeException("송금 처리 중 시스템 오류! (감사 로그는 유지됨)");
    }
} 