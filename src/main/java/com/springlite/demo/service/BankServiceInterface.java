package com.springlite.demo.service;

import java.math.BigDecimal;

/**
 * 🏦 Bank Service Interface
 * JDK Dynamic Proxy를 위한 인터페이스
 */
public interface BankServiceInterface {
    
    /**
     * 🏗️ 계좌 테이블 초기화
     */
    void initializeAccountTable();
    
    /**
     * 💰 계좌 생성
     */
    void createAccount(String accountNumber, String accountName, BigDecimal initialBalance);
    
    /**
     * 💵 잔액 조회
     */
    BigDecimal getBalance(String accountNumber);
    
    /**
     * 🔄 송금 (성공 시나리오)
     */
    void transferMoney(String fromAccount, String toAccount, BigDecimal amount);
    
    /**
     * 💥 송금 실패 시나리오 (롤백 테스트)
     */
    void transferMoneyWithFailure(String fromAccount, String toAccount, BigDecimal amount);
    
    /**
     * 📊 모든 계좌 정보 조회
     */
    void printAllAccounts();
    
    // REQUIRES_NEW 테스트 메서드들
    void transferMoneyWithAudit(String fromAccount, String toAccount, BigDecimal amount);
    
    void transferMoneyWithAuditAndFailure(String fromAccount, String toAccount, BigDecimal amount);
} 