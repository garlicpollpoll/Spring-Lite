package com.springlite.framework.transaction;

import java.sql.Connection;

/**
 * 🔄 Transaction Status Interface
 * 트랜잭션의 현재 상태를 나타내는 인터페이스
 */
public interface TransactionStatus {
    
    /**
     * 새로운 트랜잭션인지 여부
     */
    boolean isNewTransaction();
    
    /**
     * 롤백만 가능한 상태인지 여부
     */
    boolean isRollbackOnly();
    
    /**
     * 롤백만 가능하도록 설정
     */
    void setRollbackOnly();
    
    /**
     * 트랜잭션이 완료되었는지 여부
     */
    boolean isCompleted();
    
    /**
     * 내부적으로 완료 상태 설정
     */
    void setCompleted();
    
    /**
     * 트랜잭션과 연결된 Connection 반환
     */
    Connection getConnection();
    
    /**
     * Connection 설정
     */
    void setConnection(Connection connection);
    
    /**
     * REQUIRES_NEW 전파 속성 여부 확인
     */
    boolean isRequiresNew();
} 