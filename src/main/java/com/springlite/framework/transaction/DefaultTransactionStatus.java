package com.springlite.framework.transaction;

import java.sql.Connection;

/**
 * 🔄 Default Transaction Status
 * TransactionStatus의 기본 구현체
 */
public class DefaultTransactionStatus implements TransactionStatus {
    
    private boolean newTransaction;
    private boolean rollbackOnly = false;
    private boolean completed = false;
    private boolean requiresNew = false;  // REQUIRES_NEW 전파 속성 플래그
    private Connection connection;
    
    public DefaultTransactionStatus(Connection connection, boolean newTransaction) {
        this.connection = connection;
        this.newTransaction = newTransaction;
    }
    
    @Override
    public boolean isNewTransaction() {
        return newTransaction;
    }
    
    @Override
    public boolean isRollbackOnly() {
        return rollbackOnly;
    }
    
    @Override
    public void setRollbackOnly() {
        this.rollbackOnly = true;
    }
    
    @Override
    public boolean isCompleted() {
        return completed;
    }
    
    @Override
    public void setCompleted() {
        this.completed = true;
    }
    
    @Override
    public Connection getConnection() {
        return connection;
    }
    
    @Override
    public void setConnection(Connection connection) {
        this.connection = connection;
    }
    
    /**
     * REQUIRES_NEW 전파 속성 여부 확인
     */
    public boolean isRequiresNew() {
        return requiresNew;
    }
    
    /**
     * REQUIRES_NEW 전파 속성 설정
     */
    public void setRequiresNew(boolean requiresNew) {
        this.requiresNew = requiresNew;
    }
    
    @Override
    public String toString() {
        return "DefaultTransactionStatus{" +
                "newTransaction=" + newTransaction +
                ", rollbackOnly=" + rollbackOnly +
                ", completed=" + completed +
                ", requiresNew=" + requiresNew +
                ", connection=" + (connection != null ? connection.getClass().getSimpleName() : "null") +
                '}';
    }
} 