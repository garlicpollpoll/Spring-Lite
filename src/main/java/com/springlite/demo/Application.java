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
            System.out.println("\nAvailable endpoints:");
            System.out.println("- GET http://localhost:8080/api/users/hello");
            System.out.println("- GET http://localhost:8080/api/users/test");
            System.out.println("- GET http://localhost:8080/api/users");
            System.out.println("\nPress Ctrl+C to stop the server");
            
            // 서버가 종료될 때까지 대기
            server.join();
            
        } catch (Exception e) {
            System.err.println("Failed to start application: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 