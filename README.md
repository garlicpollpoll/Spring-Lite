# Spring Lite Framework

순수 자바로 구현한 간단한 스프링 프레임워크

## 주요 기능

1. **AnnotationApplicationContext** - 어노테이션 기반 애플리케이션 컨텍스트
2. **DispatcherServlet** - 웹 요청 처리 서블릿  
3. **Annotation & Reflection** - 어노테이션 기반 컴포넌트 스캔
4. **Proxy Pattern (AOP)** - @Transactional 어노테이션 지원
5. **Bean Lifecycle** - 빈 생명주기 관리
6. **IoC & DI** - 제어 반전과 의존성 주입

## 실행 방법 (일반 애플리케이션)

```bash
./gradlew run
```

## 실행 방법 (AopTest)

```bash
./gradlew runAopTest
```

## 실행 방법 (BeanLifeCycleTest)
```bash
./gradlew runLifecycleTest
```

## 프로젝트 구조

- `src/main/java/com/springlite/framework/` - 프레임워크 코어
- `src/main/java/com/springlite/demo/` - 데모 애플리케이션 
