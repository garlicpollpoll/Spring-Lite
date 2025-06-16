package com.springlite.framework.transaction;

/**
 * 🔄 Transaction Manager Interface
 * Spring Framework의 PlatformTransactionManager를 참고하여 Spring Lite용으로 간단히 구현
 * 
 * 트랜잭션의 시작, 커밋, 롤백을 관리하는 인터페이스
 */
public interface TransactionManager {
    
    /**
     * 새로운 트랜잭션을 시작하거나 기존 트랜잭션에 참여합니다
     * 
     * @param definition 트랜잭션 정의 (읽기 전용, 타임아웃 등)
     * @return 트랜잭션 상태 정보
     * @throws TransactionException 트랜잭션 시작 실패 시
     */
    TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException;
    
    /**
     * 트랜잭션을 커밋합니다
     * 
     * @param status 트랜잭션 상태
     * @throws TransactionException 커밋 실패 시
     */
    void commit(TransactionStatus status) throws TransactionException;
    
    /**
     * 트랜잭션을 롤백합니다
     * 
     * @param status 트랜잭션 상태
     * @throws TransactionException 롤백 실패 시
     */
    void rollback(TransactionStatus status) throws TransactionException;
} 