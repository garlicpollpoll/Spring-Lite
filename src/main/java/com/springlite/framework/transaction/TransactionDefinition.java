package com.springlite.framework.transaction;

/**
 * 🔄 Transaction Definition Interface
 * 트랜잭션의 속성을 정의하는 인터페이스
 */
public interface TransactionDefinition {
    
    /**
     * 트랜잭션 전파 속성
     */
    Propagation getPropagation();
    
    /**
     * 읽기 전용 트랜잭션 여부
     */
    boolean isReadOnly();
    
    /**
     * 트랜잭션 타임아웃 (초 단위)
     */
    int getTimeout();
    
    /**
     * 롤백할 예외 클래스들
     */
    Class<? extends Throwable>[] getRollbackFor();
    
    /**
     * 롤백하지 않을 예외 클래스들
     */
    Class<? extends Throwable>[] getNoRollbackFor();
} 