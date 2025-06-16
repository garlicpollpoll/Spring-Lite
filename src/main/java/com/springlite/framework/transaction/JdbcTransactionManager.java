package com.springlite.framework.transaction;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Stack;

/**
 * 🔄 JDBC Transaction Manager
 * Spring Framework의 DataSourceTransactionManager를 참고하여 구현
 * 
 * JDBC Connection을 사용하여 트랜잭션을 관리합니다.
 * ACID 속성을 보장합니다:
 * - Atomicity (원자성): 트랜잭션의 모든 작업이 성공하거나 모두 실패
 * - Consistency (일관성): 트랜잭션 전후 데이터의 일관성 유지
 * - Isolation (고립성): 동시 실행되는 트랜잭션들이 서로 격리
 * - Durability (지속성): 커밋된 트랜잭션은 영구적으로 저장
 * 
 * 트랜잭션 전파 속성 지원:
 * - REQUIRED: 기존 트랜잭션에 참여하거나 새로 생성
 * - REQUIRES_NEW: 항상 새로운 트랜잭션 생성, 기존 트랜잭션 일시 중단
 */
public class JdbcTransactionManager implements TransactionManager {
    
    private DataSource dataSource;
    
    // 현재 스레드의 트랜잭션 상태를 저장
    private static final ThreadLocal<TransactionStatus> currentTransaction = new ThreadLocal<>();
    
    // REQUIRES_NEW를 위한 중단된 트랜잭션 스택
    private static final ThreadLocal<Stack<TransactionStatus>> suspendedTransactions = new ThreadLocal<>();
    
    public JdbcTransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
        System.out.println("🔄 JdbcTransactionManager 생성됨 with DataSource: " + dataSource.getClass().getSimpleName());
    }
    
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    public DataSource getDataSource() {
        return dataSource;
    }
    
    @Override
    public TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException {
        System.out.println("🚀 트랜잭션 시작 요청: " + definition);
        
        try {
            // 기존 트랜잭션 확인
            TransactionStatus existingTransaction = currentTransaction.get();
            
            // 전파 속성에 따른 처리
            switch (definition.getPropagation()) {
                case REQUIRED:
                    return handleRequired(existingTransaction, definition);
                    
                case REQUIRES_NEW:
                    return handleRequiresNew(existingTransaction, definition);
                    
                default:
                    throw new TransactionException("지원하지 않는 전파 속성: " + definition.getPropagation());
            }
            
        } catch (SQLException e) {
            System.err.println("❌ 트랜잭션 시작 실패: " + e.getMessage());
            throw new TransactionException("트랜잭션 시작 실패", e);
        }
    }
    
    /**
     * REQUIRED 전파 속성 처리
     * 기존 트랜잭션이 있으면 참여하고, 없으면 새로 생성
     */
    private TransactionStatus handleRequired(TransactionStatus existingTransaction, TransactionDefinition definition) throws SQLException {
        if (existingTransaction != null && !existingTransaction.isCompleted()) {
            System.out.println("📝 기존 트랜잭션에 참여: " + existingTransaction);
            return existingTransaction;
        }
        
        // 새로운 트랜잭션 생성
        return createNewTransaction(definition, false);
    }
    
    /**
     * REQUIRES_NEW 전파 속성 처리
     * 항상 새로운 트랜잭션을 생성하고, 기존 트랜잭션이 있으면 일시 중단
     */
    private TransactionStatus handleRequiresNew(TransactionStatus existingTransaction, TransactionDefinition definition) throws SQLException {
        // 기존 트랜잭션이 있으면 일시 중단
        if (existingTransaction != null && !existingTransaction.isCompleted()) {
            System.out.println("⏸️ 기존 트랜잭션 일시 중단: " + existingTransaction);
            suspendTransaction(existingTransaction);
        }
        
        // 새로운 트랜잭션 생성
        return createNewTransaction(definition, true);
    }
    
    /**
     * 새로운 트랜잭션 생성
     */
    private TransactionStatus createNewTransaction(TransactionDefinition definition, boolean isRequiresNew) throws SQLException {
        // 새로운 Connection 가져오기
        Connection connection = dataSource.getConnection();
        
        // ✅ ACID의 A (Atomicity) & I (Isolation) 보장
        connection.setAutoCommit(false);  // 자동 커밋 비활성화로 원자성 보장
        
        // 읽기 전용 설정
        if (definition.isReadOnly()) {
            connection.setReadOnly(true);
            System.out.println("📖 읽기 전용 트랜잭션 설정");
        }
        
        // 트랜잭션 격리 수준 설정 (기본: READ_COMMITTED)
        connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        
        // 새로운 트랜잭션 상태 생성
        DefaultTransactionStatus status = new DefaultTransactionStatus(connection, true);
        status.setRequiresNew(isRequiresNew);  // REQUIRES_NEW 표시
        currentTransaction.set(status);
        
        String txType = isRequiresNew ? "🆕 REQUIRES_NEW" : "🔄 REQUIRED";
        System.out.println("✅ " + txType + " 트랜잭션 시작됨: " + status);
        return status;
    }
    
    /**
     * 트랜잭션 일시 중단 (REQUIRES_NEW용)
     */
    private void suspendTransaction(TransactionStatus transaction) {
        // 중단된 트랜잭션 스택에 추가
        Stack<TransactionStatus> stack = suspendedTransactions.get();
        if (stack == null) {
            stack = new Stack<>();
            suspendedTransactions.set(stack);
        }
        stack.push(transaction);
        
        // 현재 트랜잭션에서 제거
        currentTransaction.remove();
        System.out.println("⏸️ 트랜잭션 중단됨: " + transaction);
    }
    
    /**
     * 중단된 트랜잭션 재개 (REQUIRES_NEW 완료 후)
     */
    private void resumeTransaction() {
        Stack<TransactionStatus> stack = suspendedTransactions.get();
        if (stack != null && !stack.isEmpty()) {
            TransactionStatus suspendedTransaction = stack.pop();
            currentTransaction.set(suspendedTransaction);
            System.out.println("▶️ 트랜잭션 재개: " + suspendedTransaction);
            
            // 스택이 비어있으면 ThreadLocal 정리
            if (stack.isEmpty()) {
                suspendedTransactions.remove();
            }
        }
    }
    
    @Override
    public void commit(TransactionStatus status) throws TransactionException {
        System.out.println("💾 트랜잭션 커밋 시도: " + status);
        
        if (status.isCompleted()) {
            System.out.println("⚠️ 이미 완료된 트랜잭션입니다");
            return;
        }
        
        if (status.isRollbackOnly()) {
            System.out.println("🔄 롤백 전용 트랜잭션이므로 롤백 수행");
            rollback(status);
            return;
        }
        
        try {
            Connection connection = status.getConnection();
            if (connection != null) {
                // ✅ ACID의 D (Durability) 보장 - 커밋으로 영구 저장
                connection.commit();
                System.out.println("✅ 트랜잭션 커밋 성공");
            }
        } catch (SQLException e) {
            System.err.println("❌ 트랜잭션 커밋 실패: " + e.getMessage());
            throw new TransactionException("트랜잭션 커밋 실패", e);
        } finally {
            cleanupTransaction(status);
        }
    }
    
    @Override
    public void rollback(TransactionStatus status) throws TransactionException {
        System.out.println("🔄 트랜잭션 롤백 시도: " + status);
        
        if (status.isCompleted()) {
            System.out.println("⚠️ 이미 완료된 트랜잭션입니다");
            return;
        }
        
        try {
            Connection connection = status.getConnection();
            if (connection != null) {
                // ✅ ACID의 A (Atomicity) 보장 - 롤백으로 원자성 유지
                connection.rollback();
                System.out.println("✅ 트랜잭션 롤백 성공");
            }
        } catch (SQLException e) {
            System.err.println("❌ 트랜잭션 롤백 실패: " + e.getMessage());
            throw new TransactionException("트랜잭션 롤백 실패", e);
        } finally {
            cleanupTransaction(status);
        }
    }
    
    /**
     * 트랜잭션 정리 (Connection 닫기, ThreadLocal 정리)
     */
    private void cleanupTransaction(TransactionStatus status) {
        try {
            Connection connection = status.getConnection();
            if (connection != null && !connection.isClosed()) {
                // Connection 원래 상태로 복원
                connection.setAutoCommit(true);
                connection.setReadOnly(false);
                connection.close();
                System.out.println("🧹 Connection 정리 완료");
            }
        } catch (SQLException e) {
            System.err.println("⚠️ Connection 정리 중 오류: " + e.getMessage());
        } finally {
            status.setCompleted();
            
            // REQUIRES_NEW 트랜잭션이 완료되면 중단된 트랜잭션 재개
            if (status.isRequiresNew()) {
                currentTransaction.remove();
                resumeTransaction();
                System.out.println("🔄 REQUIRES_NEW 트랜잭션 완료, 이전 트랜잭션 재개");
            } else {
                currentTransaction.remove();
            }
            
            System.out.println("🧹 트랜잭션 ThreadLocal 정리 완료");
        }
    }
    
    /**
     * 현재 트랜잭션 상태 반환 (Spring Lite 내부용)
     */
    public static TransactionStatus getCurrentTransaction() {
        return currentTransaction.get();
    }
    
    /**
     * 예외가 롤백 대상인지 확인
     */
    public boolean shouldRollback(Throwable ex, TransactionDefinition definition) {
        // 명시적으로 롤백하지 않을 예외인지 확인
        for (Class<? extends Throwable> noRollbackClass : definition.getNoRollbackFor()) {
            if (noRollbackClass.isAssignableFrom(ex.getClass())) {
                return false;
            }
        }
        
        // 명시적으로 롤백할 예외인지 확인
        for (Class<? extends Throwable> rollbackClass : definition.getRollbackFor()) {
            if (rollbackClass.isAssignableFrom(ex.getClass())) {
                return true;
            }
        }
        
        // 기본적으로 RuntimeException과 Error는 롤백
        return (ex instanceof RuntimeException) || (ex instanceof Error);
    }
} 