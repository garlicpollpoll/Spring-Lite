package com.springlite.demo;

import com.springlite.demo.dto.User;
import com.springlite.demo.service.UserService;
import com.springlite.framework.annotations.ComponentScan;
import com.springlite.framework.annotations.Configuration;
import com.springlite.framework.context.AnnotationApplicationContext;

/**
 * 🎭 AOP 기능을 테스트하는 애플리케이션
 */
@Configuration
@ComponentScan(basePackages = "com.springlite.demo")
public class AopTestApp {
    
    public static void main(String[] args) {
        System.out.println("🚀 AOP 테스트 애플리케이션 시작");
        System.out.println("================================");
        
        try (AnnotationApplicationContext context = new AnnotationApplicationContext(AopTestApp.class)) {
            
            UserService userService = context.getBean(UserService.class);
            
            System.out.println("\n🔥 1. 정상적인 사용자 생성 테스트");
            System.out.println("─────────────────────────────");
            User newUser = userService.createUser("김춘식", "chunsik@example.com");
            System.out.println("생성된 사용자: " + newUser);
            
            System.out.println("\n🔥 2. 사용자 조회 테스트 (@Around 실행 시간 측정)");
            System.out.println("──────────────────────────────────────────");
            User foundUser = userService.getUserById(1L);
            System.out.println("조회된 사용자: " + foundUser);
            
            System.out.println("\n🔥 3. 사용자 업데이트 테스트");
            System.out.println("─────────────────────────");
            User updatedUser = userService.updateUser(1L, "김춘식", "chunsik.kim@example.com");
            System.out.println("업데이트된 사용자: " + updatedUser);
            
            System.out.println("\n🔥 4. 예외 발생 테스트 (@AfterThrowing 실행)");
            System.out.println("──────────────────────────────────────");
            try {
                userService.throwException("테스트 예외 메시지");
            } catch (Exception e) {
                System.out.println("예외가 잡혔습니다: " + e.getMessage());
            }
            
            System.out.println("\n🔥 5. 존재하지 않는 사용자 조회 테스트");
            System.out.println("───────────────────────────────────");
            try {
                userService.getUserById(999L);
            } catch (Exception e) {
                System.out.println("예외가 잡혔습니다: " + e.getMessage());
            }
            
            System.out.println("\n🔥 6. 사용자 삭제 테스트");
            System.out.println("─────────────────────");
            userService.deleteUser(1L);
            System.out.println("사용자 삭제 완료");
            
            System.out.println("\n✅ AOP 테스트 완료!");
            System.out.println("================================");
            
        } catch (Exception e) {
            System.err.println("❌ 애플리케이션 실행 중 오류 발생:");
            e.printStackTrace();
        }
    }
} 