package com.springlite.demo.aspect;

import com.springlite.framework.aop.JoinPoint;
import com.springlite.framework.aop.ProceedingJoinPoint;
import com.springlite.framework.aop.annotations.*;
import com.springlite.framework.annotations.Component;

/**
 * 🎭 로깅 Aspect - 다양한 Advice 타입을 보여주는 예제
 */
@Aspect
@Component
public class LoggingAspect {
    
    /**
     * 서비스 메서드들을 매칭하는 명명된 Pointcut
     */
    @Pointcut("execution(* com.springlite.demo.service.*.*(..))")
    public void serviceOperation() {}
    
    /**
     * 모든 public 메서드를 매칭하는 Pointcut
     */
    @Pointcut("execution(public * *(..))")
    public void publicMethod() {}
    
    /**
     * @Before 어드바이스 - 메서드 실행 전에 실행
     */
    @Before("serviceOperation()")
    public void logMethodEntry(JoinPoint joinPoint) {
        System.out.println("🟢 [BEFORE] 메서드 시작: " + joinPoint.getSignature());
        System.out.println("   📥 파라미터: " + java.util.Arrays.toString(joinPoint.getArgs()));
    }
    
    /**
     * @After 어드바이스 - 메서드 실행 후 항상 실행 (finally와 같음)
     */
    @After("serviceOperation()")
    public void logMethodExit(JoinPoint joinPoint) {
        System.out.println("🔴 [AFTER] 메서드 종료: " + joinPoint.getSignature());
    }
    
    /**
     * @AfterReturning 어드바이스 - 메서드가 정상적으로 반환된 후 실행
     */
    @AfterReturning(pointcut = "serviceOperation()", returning = "result")
    public void logMethodReturn(JoinPoint joinPoint, Object result) {
        System.out.println("✅ [AFTER_RETURNING] 메서드 성공: " + joinPoint.getSignature());
        System.out.println("   📤 반환값: " + result);
    }
    
    /**
     * @AfterThrowing 어드바이스 - 메서드에서 예외가 발생한 후 실행
     */
    @AfterThrowing(pointcut = "serviceOperation()", throwing = "ex")
    public void logMethodException(JoinPoint joinPoint, Throwable ex) {
        System.out.println("❌ [AFTER_THROWING] 메서드 예외: " + joinPoint.getSignature());
        System.out.println("   💥 예외: " + ex.getClass().getSimpleName() + " - " + ex.getMessage());
    }
    
    /**
     * @Around 어드바이스 - 메서드 실행을 완전히 제어
     */
    @Around("execution(* com.springlite.demo.service.UserService.getUserById(..))")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        System.out.println("⏱️  [AROUND] 실행 시간 측정 시작: " + joinPoint.getSignature());
        
        try {
            // 실제 메서드 실행
            Object result = joinPoint.proceed();
            
            long executionTime = System.currentTimeMillis() - startTime;
            System.out.println("⏱️  [AROUND] 실행 시간: " + executionTime + "ms");
            
            return result;
        } catch (Throwable ex) {
            long executionTime = System.currentTimeMillis() - startTime;
            System.out.println("⏱️  [AROUND] 실행 시간 (예외): " + executionTime + "ms");
            throw ex;
        }
    }
    
    /**
     * 빈 패턴을 사용한 Pointcut - *Service로 끝나는 빈들
     */
    @Before("bean(*Service)")
    public void logServiceCall(JoinPoint joinPoint) {
        System.out.println("🎯 [SERVICE_CALL] 서비스 호출: " + joinPoint.getTarget().getClass().getSimpleName() 
                         + "." + joinPoint.getMethod().getName());
    }
} 