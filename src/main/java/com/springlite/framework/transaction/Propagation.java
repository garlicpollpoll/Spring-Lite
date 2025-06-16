package com.springlite.framework.transaction;

/**
 * 🔄 Transaction Propagation
 * Spring Framework의 Propagation을 참고하여 구현
 * 
 * 트랜잭션의 전파 방식을 정의합니다.
 * 기존 트랜잭션이 있을 때 새로운 트랜잭션을 어떻게 처리할지 결정합니다.
 */
public enum Propagation {
    
    /**
     * 기존 트랜잭션이 있으면 참여하고, 없으면 새로 생성 (기본값)
     * Support a current transaction, create a new one if none exists.
     */
    REQUIRED(0),
    
    /**
     * 항상 새로운 트랜잭션을 생성
     * 기존 트랜잭션이 있으면 일시 중단하고 새 트랜잭션 시작
     * Create a new transaction, and suspend the current transaction if one exists.
     */
    REQUIRES_NEW(3);
    
    private final int value;
    
    Propagation(int value) {
        this.value = value;
    }
    
    public int value() {
        return this.value;
    }
    
    @Override
    public String toString() {
        return name() + "(" + value + ")";
    }
} 