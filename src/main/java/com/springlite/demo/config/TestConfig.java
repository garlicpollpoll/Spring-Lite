package com.springlite.demo.config;

import com.springlite.framework.annotations.Configuration;
import com.springlite.framework.annotations.Bean;

/**
 * 🔧 @Bean 테스트를 위한 Configuration 클래스
 */
@Configuration
public class TestConfig {
    
    @Bean
    public DatabaseService databaseService() {
        System.out.println("🔧 @Bean: DatabaseService 생성 중...");
        return new DatabaseService("jdbc:mysql://localhost:3306/test");
    }
    
    @Bean(initMethod = "customInit", destroyMethod = "customDestroy")
    public CacheService cacheService() {
        System.out.println("🔧 @Bean: CacheService 생성 중...");
        return new CacheService(100);
    }
    
    @Bean("customName")
    public NotificationService notificationService(DatabaseService databaseService) {
        System.out.println("🔧 @Bean: NotificationService 생성 중... (의존성: " + databaseService + ")");
        return new NotificationService(databaseService);
    }
    
    // 테스트용 서비스 클래스들
    public static class DatabaseService {
        private String connectionUrl;
        
        public DatabaseService(String connectionUrl) {
            this.connectionUrl = connectionUrl;
            System.out.println("💾 DatabaseService 생성: " + connectionUrl);
        }
        
        @Override
        public String toString() {
            return "DatabaseService{url='" + connectionUrl + "'}";
        }
    }
    
    public static class CacheService {
        private int maxSize;
        
        public CacheService(int maxSize) {
            this.maxSize = maxSize;
            System.out.println("🗄️  CacheService 생성: maxSize=" + maxSize);
        }
        
        public void customInit() {
            System.out.println("🚀 CacheService.customInit() 호출됨!");
        }
        
        public void customDestroy() {
            System.out.println("🛑 CacheService.customDestroy() 호출됨!");
        }
        
        @Override
        public String toString() {
            return "CacheService{maxSize=" + maxSize + "}";
        }
    }
    
    public static class NotificationService {
        private DatabaseService databaseService;
        
        public NotificationService(DatabaseService databaseService) {
            this.databaseService = databaseService;
            System.out.println("📧 NotificationService 생성: " + databaseService);
        }
        
        @Override
        public String toString() {
            return "NotificationService{db=" + databaseService + "}";
        }
    }
} 