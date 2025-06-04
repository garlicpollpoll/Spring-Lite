package com.springlite.framework.proxy;

import com.springlite.framework.annotations.Transactional;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ProxyFactory {
    
    public Object createProxy(Object target) {
        Class<?> targetClass = target.getClass();
        Class<?>[] interfaces = targetClass.getInterfaces();
        
        if (interfaces.length > 0) {
            // JDK 동적 프록시 사용 (인터페이스 기반)
            return Proxy.newProxyInstance(
                targetClass.getClassLoader(),
                interfaces,
                new TransactionalInvocationHandler(target)
            );
        } else {
            // CGLIB을 사용해야 하지만 간단하게 원본 객체 반환
            // 실제로는 CGLIB 라이브러리를 사용해서 클래스 기반 프록시를 생성해야 함
            System.out.println("Warning: Cannot create proxy for class without interfaces: " + targetClass.getName());
            return target;
        }
    }
    
    private static class TransactionalInvocationHandler implements InvocationHandler {
        private final Object target;
        
        public TransactionalInvocationHandler(Object target) {
            this.target = target;
        }
        
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // @Transactional 어노테이션 확인
            Transactional transactional = method.getAnnotation(Transactional.class);
            if (transactional == null) {
                transactional = target.getClass().getAnnotation(Transactional.class);
            }
            
            if (transactional != null) {
                return executeWithTransaction(method, args, transactional);
            } else {
                return method.invoke(target, args);
            }
        }
        
        private Object executeWithTransaction(Method method, Object[] args, Transactional transactional) throws Throwable {
            System.out.println("Starting transaction for method: " + method.getName());
            System.out.println("Transaction config - readOnly: " + transactional.readOnly() + 
                             ", propagation: " + transactional.propagation());
            
            try {
                // 실제 메소드 실행
                Object result = method.invoke(target, args);
                
                System.out.println("Committing transaction for method: " + method.getName());
                return result;
            } catch (Exception e) {
                System.out.println("Rolling back transaction for method: " + method.getName() + 
                                 " due to exception: " + e.getCause().getClass().getSimpleName());
                throw e.getCause();
            }
        }
    }
} 