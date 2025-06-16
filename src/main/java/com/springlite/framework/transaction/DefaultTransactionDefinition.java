package com.springlite.framework.transaction;

import java.util.Arrays;

/**
 * 🔄 Default Transaction Definition
 * TransactionDefinition의 기본 구현체
 */
public class DefaultTransactionDefinition implements TransactionDefinition {
    
    private Propagation propagation = Propagation.REQUIRED;
    private boolean readOnly = false;
    private int timeout = -1;
    private Class<? extends Throwable>[] rollbackFor = new Class[0];
    private Class<? extends Throwable>[] noRollbackFor = new Class[0];
    
    /**
     * 기본 생성자
     */
    public DefaultTransactionDefinition() {
    }
    
    /**
     * @Transactional 어노테이션으로부터 생성
     */
    public DefaultTransactionDefinition(Transactional transactional) {
        this.propagation = transactional.propagation();
        this.readOnly = transactional.readOnly();
        this.timeout = transactional.timeout();
        this.rollbackFor = transactional.rollbackFor();
        this.noRollbackFor = transactional.noRollbackFor();
    }
    
    @Override
    public Propagation getPropagation() {
        return propagation;
    }
    
    public void setPropagation(Propagation propagation) {
        this.propagation = propagation;
    }
    
    @Override
    public boolean isReadOnly() {
        return readOnly;
    }
    
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }
    
    @Override
    public int getTimeout() {
        return timeout;
    }
    
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
    
    @Override
    public Class<? extends Throwable>[] getRollbackFor() {
        return rollbackFor;
    }
    
    public void setRollbackFor(Class<? extends Throwable>... rollbackFor) {
        this.rollbackFor = rollbackFor;
    }
    
    @Override
    public Class<? extends Throwable>[] getNoRollbackFor() {
        return noRollbackFor;
    }
    
    public void setNoRollbackFor(Class<? extends Throwable>... noRollbackFor) {
        this.noRollbackFor = noRollbackFor;
    }
    
    @Override
    public String toString() {
        return "DefaultTransactionDefinition{" +
                "propagation=" + propagation +
                ", readOnly=" + readOnly +
                ", timeout=" + timeout +
                ", rollbackFor=" + Arrays.toString(rollbackFor) +
                ", noRollbackFor=" + Arrays.toString(noRollbackFor) +
                '}';
    }
} 