package com.springlite.demo;

import com.springlite.demo.config.AppConfig;
import com.springlite.framework.context.AnnotationApplicationContext;
import com.springlite.framework.web.DispatcherServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class Application {
    
    public static void main(String[] args) {
        try {
            System.out.println("=== Spring Lite Framework Demo ===");
            
            // 1. ApplicationContext 생성
            System.out.println("\n1. Creating ApplicationContext...");
            AnnotationApplicationContext applicationContext = new AnnotationApplicationContext(AppConfig.class);
            
            // 2. DispatcherServlet 생성
            System.out.println("\n2. Creating DispatcherServlet...");
            DispatcherServlet dispatcherServlet = new DispatcherServlet(applicationContext);
            
            // 3. Jetty 서버 설정
            System.out.println("\n3. Starting Jetty Server...");
            Server server = new Server(8080);
            
            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
            context.setContextPath("/");
            server.setHandler(context);
            
            // DispatcherServlet 등록
            ServletHolder servletHolder = new ServletHolder(dispatcherServlet);
            context.addServlet(servletHolder, "/*");
            
            // 4. 서버 시작
            server.start();
            
            System.out.println("\n=== Server Started Successfully! ===");
            System.out.println("Server running on: http://localhost:8080");
            System.out.println("\n🚀 REST API Endpoints:");
            System.out.println("- GET http://localhost:8080/api/users/hello");
            System.out.println("- GET http://localhost:8080/api/users/test");
            System.out.println("- GET http://localhost:8080/api/users");
            System.out.println("\n🎨 MVC Web Pages:");
            System.out.println("- GET http://localhost:8080/api/users/view (사용자 목록 JSP)");
            System.out.println("- GET http://localhost:8080/api/users/detail (사용자 상세 JSP)");
            System.out.println("\n✨ 이제 진짜 Spring MVC 패턴이 완성되었습니다!");
            System.out.println("- Model: UserService, User 클래스");
            System.out.println("- View: JSP 파일들 (/WEB-INF/views/)");
            System.out.println("- Controller: UserController");
            System.out.println("\n⚠️  현재는 JSP 의존성 이슈로 일단 기본 설정으로 실행");
            System.out.println("Press Ctrl+C to stop the server");
            
            // 서버가 종료될 때까지 대기
            server.join();
            
        } catch (Exception e) {
            System.err.println("Failed to start application: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 