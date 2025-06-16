package com.springlite.framework.transaction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 🔄 @Transactional 어노테이션
 * Spring Framework의 @Transactional을 참고하여 Spring Lite용으로 간단히 구현
 * 
 * 메서드나 클래스에 이 어노테이션을 붙이면 트랜잭션 경계 내에서 실행됩니다.
 * 
 * 사용 예제:
 * @Transactional
 * public void transferMoney(String fromAccount, String toAccount, BigDecimal amount) {
 *     // 이 메서드는 트랜잭션 내에서 실행됩니다
 *     // 예외 발생 시 자동으로 롤백됩니다
 * }
 * 
 * @Transactional(propagation = Propagation.REQUIRES_NEW)
 * public void auditLog(String message) {
 *     // 이 메서드는 항상 새로운 독립적인 트랜잭션에서 실행됩니다
 * }
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Transactional {
    
    /**
     * 트랜잭션 전파 속성
     * 기존 트랜잭션이 있을 때 어떻게 처리할지 결정합니다
     */
    Propagation propagation() default Propagation.REQUIRED;
    
    /**
     * 읽기 전용 트랜잭션 여부
     * true인 경우 데이터 수정이 불가능하며 성능 최적화가 가능합니다
     */
    boolean readOnly() default false;
    
    /**
     * 트랜잭션 타임아웃 (초 단위)
     * -1인 경우 기본 타임아웃을 사용합니다
     */
    int timeout() default -1;
    
    /**
     * 롤백할 예외 클래스들
     * 기본적으로 RuntimeException과 Error에 대해 롤백됩니다
     */
    Class<? extends Throwable>[] rollbackFor() default {};
    
    /**
     * 롤백하지 않을 예외 클래스들
     * 특정 예외에 대해서는 롤백하지 않도록 설정할 수 있습니다
     */
    Class<? extends Throwable>[] noRollbackFor() default {};
} 