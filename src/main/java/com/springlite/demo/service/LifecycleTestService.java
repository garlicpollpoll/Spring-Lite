package com.springlite.demo.service;

import com.springlite.framework.annotations.*;

/**
 * 🔄 빈 라이프사이클 테스트를 위한 서비스 클래스
 */
@Service
public class LifecycleTestService {
    
    private String status = "CREATED";
    private long startTime;
    
    @PostConstruct
    public void init() {
        startTime = System.currentTimeMillis();
        status = "INITIALIZED";
        System.out.println("🚀 LifecycleTestService 초기화 완료! (시작 시간: " + startTime + ")");
    }
    
    @PostConstruct
    public void secondInit() {
        System.out.println("🚀 LifecycleTestService 두 번째 초기화 메서드 호출!");
    }
    
    @PreDestroy
    public void cleanup() {
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        status = "DESTROYED";
        System.out.println("🛑 LifecycleTestService 정리 중... (실행 시간: " + duration + "ms)");
    }
    
    @PreDestroy
    public void finalCleanup() {
        System.out.println("🛑 LifecycleTestService 최종 정리 완료!");
    }
    
    public String getStatus() {
        return status;
    }
    
    public void doSomething() {
        System.out.println("📋 LifecycleTestService 작업 수행 중... (상태: " + status + ")");
    }
    
    @Override
    public String toString() {
        return "LifecycleTestService{status='" + status + "', startTime=" + startTime + "}";
    }
} 