package com.springlite.framework.transaction;

import com.springlite.framework.aop.annotations.Around;
import com.springlite.framework.aop.annotations.Aspect;
import com.springlite.framework.aop.annotations.Pointcut;
import com.springlite.framework.aop.ProceedingJoinPoint;

import java.lang.reflect.Method;

/**
 * 🔄 Transaction Aspect
 * Spring Framework의 TransactionInterceptor를 참고하여 구현
 * 
 * @Transactional 어노테이션이 붙은 메서드를 자동으로 트랜잭션으로 감쌉니다.
 * ACID 속성을 보장합니다:
 * - Atomicity (원자성): 메서드 실행 성공 시 커밋, 실패 시 롤백
 * - Consistency (일관성): 데이터 무결성 제약조건 유지
 * - Isolation (고립성): 트랜잭션 격리 수준 설정
 * - Durability (지속성): 커밋된 데이터의 영구 저장
 */
@Aspect
public class TransactionAspect {
    
    private TransactionManager transactionManager;
    
    /**
     * TransactionManager 주입 (생성자 주입)
     */
    public TransactionAspect(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }
    
    /**
     * @Transactional 어노테이션이 붙은 메서드를 대상으로 합니다.
     * 메서드 또는 클래스 레벨에 어노테이션이 있으면 포인트컷에 매칭됩니다.
     */
    @Pointcut("@annotation(com.springlite.framework.transaction.Transactional) || @within(com.springlite.framework.transaction.Transactional)")
    public void transactionalMethods() {
    }
    
    /**
     * 트랜잭션 관리 Around Advice
     * 메서드 실행 전후에 트랜잭션을 시작하고 커밋/롤백합니다.
     */
    @Around("transactionalMethods()")
    public Object manageTransaction(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("🔥 TransactionAspect.manageTransaction() 호출됨! 메서드: " + joinPoint.getMethod().getName());
        
        Transactional transactional = getTransactionalAnnotation(joinPoint);
        if (transactional == null) {
            System.out.println("⚠️ @Transactional 어노테이션을 찾을 수 없음");
            // @Transactional이 없으면 그냥 메서드 실행
            return joinPoint.proceed();
        }
        
        System.out.println("🎯 @Transactional 발견: readOnly=" + transactional.readOnly());
        
        // 트랜잭션 정의 생성
        TransactionDefinition definition = new DefaultTransactionDefinition(transactional);
        
        // 트랜잭션 시작
        System.out.println("🚀 트랜잭션 시작 중...");
        TransactionStatus status = transactionManager.getTransaction(definition);
        System.out.println("✅ 트랜잭션 시작됨: " + status);
        
        try {
            // 실제 메서드 실행
            System.out.println("🔄 메서드 실행 중: " + joinPoint.getMethod().getName());
            Object result = joinPoint.proceed();
            System.out.println("✅ 메서드 실행 완료");
            
            // 성공 시 커밋
            System.out.println("💾 트랜잭션 커밋 중...");
            transactionManager.commit(status);
            System.out.println("✅ 트랜잭션 커밋 완료");
            return result;
            
        } catch (Exception e) {
            System.out.println("💥 예외 발생: " + e.getMessage());
            // 예외 발생 시 롤백 여부 판단
            if (shouldRollback(e, transactional)) {
                System.out.println("🔄 트랜잭션 롤백 중...");
                transactionManager.rollback(status);
                System.out.println("✅ 트랜잭션 롤백 완료");
            } else {
                System.out.println("💾 예외 발생했지만 커밋 중...");
                transactionManager.commit(status);
                System.out.println("✅ 트랜잭션 커밋 완료");
            }
            throw e;
        }
    }
    
    /**
     * JoinPoint에서 @Transactional 어노테이션 추출
     */
    private Transactional getTransactionalAnnotation(ProceedingJoinPoint joinPoint) {
        Method method = joinPoint.getMethod();
        Object target = joinPoint.getTarget();
        
        // 타겟 클래스에서 동일한 메서드 찾기
        try {
            Method targetMethod = target.getClass().getMethod(method.getName(), method.getParameterTypes());
            
            // 1. 타겟 메서드 레벨에서 찾기
            Transactional transactional = targetMethod.getAnnotation(Transactional.class);
            if (transactional != null) {
                System.out.println("✅ 메서드 레벨에서 @Transactional 발견");
                return transactional;
            }
            
            // 2. 타겟 클래스 레벨에서 찾기
            transactional = target.getClass().getAnnotation(Transactional.class);
            if (transactional != null) {
                System.out.println("✅ 클래스 레벨에서 @Transactional 발견");
                return transactional;
            }
            
        } catch (NoSuchMethodException e) {
            System.out.println("⚠️ 타겟 클래스에서 메서드를 찾을 수 없음: " + method.getName());
        }
        
        // 3. 프록시 메서드에서 찾기 (백업)
        Transactional transactional = method.getAnnotation(Transactional.class);
        if (transactional != null) {
            System.out.println("✅ 프록시 메서드에서 @Transactional 발견");
            return transactional;
        }
        
        // 4. 프록시 클래스에서 찾기 (백업)
        transactional = method.getDeclaringClass().getAnnotation(Transactional.class);
        if (transactional != null) {
            System.out.println("✅ 프록시 클래스에서 @Transactional 발견");
            return transactional;
        }
        
        System.out.println("❌ 어디에서도 @Transactional을 찾을 수 없음");
        return null;
    }
    
    /**
     * 예외 발생 시 롤백 여부 판단
     */
    private boolean shouldRollback(Exception e, Transactional transactional) {
        // rollbackFor에 지정된 예외인지 확인
        for (Class<? extends Throwable> rollbackClass : transactional.rollbackFor()) {
            if (rollbackClass.isAssignableFrom(e.getClass())) {
                return true;
            }
        }
        
        // noRollbackFor에 지정된 예외인지 확인
        for (Class<? extends Throwable> noRollbackClass : transactional.noRollbackFor()) {
            if (noRollbackClass.isAssignableFrom(e.getClass())) {
                return false;
            }
        }
        
        // 기본적으로 RuntimeException과 Error는 롤백
        return e instanceof RuntimeException;
    }
}
 