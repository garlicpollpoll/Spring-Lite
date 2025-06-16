package com.springlite.framework.transaction;

/**
 * 🚨 Transaction Exception
 * 트랜잭션 관련 예외를 나타내는 클래스
 */
public class TransactionException extends RuntimeException {
    
    public TransactionException(String message) {
        super(message);
    }
    
    public TransactionException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public TransactionException(Throwable cause) {
        super(cause);
    }
} 