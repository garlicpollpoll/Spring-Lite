package com.springlite.demo;

import com.springlite.framework.context.AnnotationApplicationContext;
import com.springlite.demo.service.LifecycleTestService;

/**
 * 🔄 @PostConstruct와 @PreDestroy 라이프사이클 테스트
 */
public class LifecycleTestApp {
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("\n🔄 Spring Lite Framework - 빈 라이프사이클 테스트\n");
        
        System.out.println("1️⃣ ApplicationContext 생성 시작...");
        AnnotationApplicationContext context = new AnnotationApplicationContext("com.springlite.demo");
        
        System.out.println("\n2️⃣ 빈 사용 중...");
        LifecycleTestService service = context.getBean(LifecycleTestService.class);
        System.out.println("현재 상태: " + service.getStatus());
        service.doSomething();
        
        System.out.println("\n3️⃣ 3초간 애플리케이션 실행 중...");
        Thread.sleep(3000);
        
        System.out.println("\n4️⃣ ApplicationContext 종료...");
        context.close();
        
        System.out.println("\n✅ 라이프사이클 테스트 완료!\n");
        
        /* 
         * 예상 출력 순서:
         * 🚀 @PostConstruct 호출: init
         * 🚀 @PostConstruct 호출: secondInit
         * (애플리케이션 사용)
         * 🛑 @PreDestroy 호출: cleanup
         * 🛑 @PreDestroy 호출: finalCleanup
         */
    }
} 