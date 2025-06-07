package com.springlite.demo;

import com.springlite.demo.config.AppConfig;
import com.springlite.framework.context.AnnotationApplicationContext;
import com.springlite.framework.web.DispatcherServlet;
import com.springlite.framework.web.InternalResourceViewResolver;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.webapp.WebAppContext;

public class Application {
    
    public static void main(String[] args) {
        try {
            System.out.println("=== Spring Lite Framework Demo ===");
            
            // 1. ApplicationContext 생성
            System.out.println("\n1. Creating ApplicationContext...");
            AnnotationApplicationContext applicationContext = new AnnotationApplicationContext(AppConfig.class);
            
            // 🔥 2. ViewResolver 설정 (실제 Spring Framework 방식)
            System.out.println("\n2. Creating ViewResolver...");
            InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
            viewResolver.setPrefix("/WEB-INF/views/");
            viewResolver.setSuffix(".jsp");
            System.out.println("ViewResolver configured: " + viewResolver);
            
            // 3. DispatcherServlet 생성 (ViewResolver와 함께)
            System.out.println("\n3. Creating DispatcherServlet with ViewResolver...");
            DispatcherServlet dispatcherServlet = new DispatcherServlet(applicationContext, viewResolver);
            
            // 4. Jetty 서버 설정 (WebAppContext with JSP support)
            System.out.println("\n4. Starting Jetty Server with JSP support...");
            Server server = new Server(8080);
            
            // 🔥 WebAppContext 사용으로 실제 JSP 엔진 지원
            WebAppContext context = new WebAppContext();
            context.setContextPath("/");
            context.setResourceBase("src/main/webapp");  // JSP 파일 위치
            context.setClassLoader(Thread.currentThread().getContextClassLoader());
            
            // JSP 지원 활성화 - 올바른 설정
            context.setConfigurationClasses(new String[]{
                "org.eclipse.jetty.webapp.WebInfConfiguration",
                "org.eclipse.jetty.webapp.WebXmlConfiguration", 
                "org.eclipse.jetty.webapp.MetaInfConfiguration",
                "org.eclipse.jetty.webapp.FragmentConfiguration",
                "org.eclipse.jetty.annotations.AnnotationConfiguration",
                "org.eclipse.jetty.webapp.JettyWebXmlConfiguration"
            });
            
            // JSP 서블릿 명시적 설정
            context.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern",
                ".*/[^/]*servlet-api-[^/]*\\.jar$|.*/javax.servlet.jsp.jstl-.*\\.jar$|.*/[^/]*taglibs.*\\.jar$");
            
            // 🔥 핵심 수정: 실제 Spring Framework와 동일한 서블릿 매핑 순서 적용!
            
            // 1️⃣ DefaultServlet: 정적 리소스 (가장 낮은 우선순위, /*에서 처리되지 않은 것들)
            ServletHolder defaultHolder = new ServletHolder("default", DefaultServlet.class);
            defaultHolder.setInitParameter("resourceBase", "src/main/webapp");
            defaultHolder.setInitParameter("dirAllowed", "false");
            defaultHolder.setInitParameter("welcomeServlets", "false");
            defaultHolder.setInitParameter("redirectWelcome", "false");
            defaultHolder.setInitParameter("gzip", "false");
            context.addServlet(defaultHolder, "/");  // Default servlet는 "/" 매핑
            
            // 2️⃣ JSP Servlet: JSP 파일 처리 (높은 우선순위)
            ServletHolder jspHolder = context.addServlet("org.apache.jasper.servlet.JspServlet", "*.jsp");
            jspHolder.setInitParameter("fork", "false");
            jspHolder.setInitParameter("xpoweredBy", "false");
            jspHolder.setInitParameter("development", "true"); // 개발 모드
            jspHolder.setInitOrder(0);  // 먼저 로드
            
            // 🔥 JSP 서블릿이 WEB-INF 경로도 처리할 수 있도록 Named 서블릿 추가
            ServletHolder jspNamedHolder = new ServletHolder("jsp", org.apache.jasper.servlet.JspServlet.class);
            jspNamedHolder.setInitParameter("fork", "false");
            jspNamedHolder.setInitParameter("xpoweredBy", "false");
            jspNamedHolder.setInitParameter("development", "true");
            jspNamedHolder.setInitOrder(0);
            context.addServlet(jspNamedHolder, "/WEB-INF/views/*");
            
            // 3️⃣ DispatcherServlet: 모든 경로 처리 (실제 Spring과 동일)
            ServletHolder dispatcherHolder = new ServletHolder("dispatcher", dispatcherServlet);
            dispatcherHolder.setInitOrder(1);  // JSP 서블릿 이후 로드
            context.addServlet(dispatcherHolder, "/*");  // 🚀 Spring과 동일하게 모든 경로!
            
            // 5. 서버 시작
            server.setHandler(context);
            server.start();
            
            System.out.println("\n=== Server Started Successfully! ===");
            System.out.println("Server running on: http://localhost:8080");
            System.out.println("\n🚀 REST API Endpoints:");
            System.out.println("- GET http://localhost:8080/users/hello");
            System.out.println("- GET http://localhost:8080/users/test");
            System.out.println("- GET http://localhost:8080/users");
            System.out.println("\n🎨 MVC Web Pages:");
            System.out.println("- GET http://localhost:8080/users/view (사용자 목록 JSP)");
            System.out.println("- GET http://localhost:8080/users/detail (사용자 상세 JSP)");
            System.out.println("\n✨ 새로 추가된 @PathVariable, @RequestParam, @RequestBody 테스트:");
            System.out.println("- GET http://localhost:8080/users/pathvar/123");
            System.out.println("- GET http://localhost:8080/users/search?name=john&page=1");
            System.out.println("- POST http://localhost:8080/users/create + JSON body");
            System.out.println("\n🎯 실제 Spring Framework와 동일한 서블릿 매핑 적용!");
            System.out.println("- DispatcherServlet: /* (모든 요청 처리, 실제 Spring과 동일)");
            System.out.println("- JSP Servlet: *.jsp (JSP 파일 처리)");
            System.out.println("- DefaultServlet: / (정적 리소스 기본 처리)");
            System.out.println("\n🔧 Spring Framework 핵심 원리:");
            System.out.println("- DispatcherServlet은 모든 요청을 받되, 해당하는 핸들러가 없으면");
            System.out.println("- JSP나 정적 리소스 요청은 적절한 서블릿으로 위임 처리");
            System.out.println("- ViewResolver: " + viewResolver + " 로 JSP 뷰 해결");
            System.out.println("Press Ctrl+C to stop the server");
            
            // 서버가 종료될 때까지 대기
            server.join();
            
        } catch (Exception e) {
            System.err.println("Failed to start application: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 