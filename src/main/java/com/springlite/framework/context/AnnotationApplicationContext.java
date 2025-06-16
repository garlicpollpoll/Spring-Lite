package com.springlite.framework.context;

import com.springlite.framework.annotations.*;
import com.springlite.framework.beans.BeanDefinition;
import com.springlite.framework.proxy.ProxyFactory;
import com.springlite.framework.aop.*;
import com.springlite.framework.aop.annotations.Aspect;
import com.springlite.framework.transaction.Transactional;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AnnotationApplicationContext implements ApplicationContext, AutoCloseable {
    
    private Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    private Map<String, Object> singletonBeans = new ConcurrentHashMap<>();
    private Map<Class<?>, String> typeToNameMap = new ConcurrentHashMap<>();
    private Set<String> creatingBeans = new HashSet<>();
    private boolean running = false;
    private ProxyFactory proxyFactory;
    
    // 🔥 새로 추가: @Configuration 클래스들을 관리
    private Map<Class<?>, Object> configurationInstances = new ConcurrentHashMap<>();
    
    // 🔥 새로 추가: AOP 관련
    private AopProxyFactory aopProxyFactory;
    private List<AspectMetadata> aspects = new ArrayList<>();
    
    /**
     * 기본 생성자 - 수동으로 빈을 등록할 때 사용
     */
    public AnnotationApplicationContext() {
        this.proxyFactory = new ProxyFactory();
        this.aopProxyFactory = new AopProxyFactory();
    }
    
    public AnnotationApplicationContext(Class<?> configClass) {
        this.proxyFactory = new ProxyFactory();
        this.aopProxyFactory = new AopProxyFactory();
        scan(configClass);
        refresh();
    }
    
    public AnnotationApplicationContext(String... basePackages) {
        this.proxyFactory = new ProxyFactory();
        this.aopProxyFactory = new AopProxyFactory();
        scanPackages(basePackages);
        refresh();
    }
    
    @Override
    public void refresh() {
        // 1. 빈 정의 스캔 (이미 완료)
        
        // @Configuration 클래스들을 먼저 인스턴스화
        instantiateConfigurationClasses();
        
        // @Bean 메서드들을 스캔해서 BeanDefinition 생성
        scanBeanMethods();
        
        // 🔥 새로 추가: @Aspect 애스펙트 스캔 및 등록
        scanAndRegisterAspects();
        
        // 2. 빈 인스턴스 생성 및 의존성 주입
        instantiateBeans();
        // 3. 애플리케이션 컨텍스트 시작
        running = true;
        System.out.println("ApplicationContext refreshed with " + beanDefinitionMap.size() + " beans and " + aspects.size() + " aspects");
    }
    
    private void scan(Class<?> configClass) {
        ComponentScan componentScan = configClass.getAnnotation(ComponentScan.class);
        if (componentScan != null) {
            String[] basePackages = componentScan.basePackages();
            if (basePackages.length == 0) {
                basePackages = componentScan.value();
            }
            if (basePackages.length == 0) {
                basePackages = new String[]{configClass.getPackage().getName()};
            }
            scanPackages(basePackages);
        } else {
            // 기본적으로 현재 패키지 스캔
            scanPackages(configClass.getPackage().getName());
        }
    }
    
    public void scanPackages(String... basePackages) {
        for (String basePackage : basePackages) {
            scanPackage(basePackage);
        }
    }
    
    private void scanPackage(String basePackage) {
        try {
            String packagePath = basePackage.replace('.', '/');
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            URL resource = classLoader.getResource(packagePath);
            
            if (resource != null) {
                File packageDir = new File(resource.getFile());
                scanDirectory(packageDir, basePackage);
            }
        } catch (Exception e) {
            System.err.println("Error scanning package: " + basePackage + " - " + e.getMessage());
        }
    }
    
    private void scanDirectory(File dir, String packageName) {
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        
        File[] files = dir.listFiles();
        if (files == null) return;
        
        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectory(file, packageName + "." + file.getName());
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + "." + file.getName().replace(".class", "");
                try {
                    Class<?> clazz = Class.forName(className);
                    if (isComponent(clazz)) {
                        registerBean(clazz);
                    }
                } catch (ClassNotFoundException e) {
                    // 클래스 로딩 실패 - 무시
                }
            }
        }
    }
    
    private boolean isComponent(Class<?> clazz) {
        return clazz.isAnnotationPresent(Component.class) ||
               clazz.isAnnotationPresent(Service.class) ||
               clazz.isAnnotationPresent(Repository.class) ||
               clazz.isAnnotationPresent(Controller.class) ||
               clazz.isAnnotationPresent(Configuration.class) ||
               clazz.isAnnotationPresent(Aspect.class);
    }
    
    private void registerBean(Class<?> clazz) {
        String beanName = getBeanName(clazz);
        BeanDefinition beanDefinition = new BeanDefinition(beanName, clazz);
        
        // Autowired 필드, 메소드, 생성자 찾기
        findAutowiredMembers(beanDefinition);
        
        // 🔥 새로 추가: 빈 라이프사이클 메서드 스캔
        scanLifecycleMethods(clazz, beanDefinition);
        
        beanDefinitionMap.put(beanName, beanDefinition);
        typeToNameMap.put(clazz, beanName);
        
        System.out.println("Registered bean: " + beanName + " of type " + clazz.getSimpleName());
    }
    
    private String getBeanName(Class<?> clazz) {
        // @Component, @Service 등의 value 값 확인
        if (clazz.isAnnotationPresent(Component.class)) {
            Component component = clazz.getAnnotation(Component.class);
            if (!component.value().isEmpty()) {
                return component.value();
            }
        }
        if (clazz.isAnnotationPresent(Service.class)) {
            Service service = clazz.getAnnotation(Service.class);
            if (!service.value().isEmpty()) {
                return service.value();
            }
        }
        if (clazz.isAnnotationPresent(Repository.class)) {
            Repository repository = clazz.getAnnotation(Repository.class);
            if (!repository.value().isEmpty()) {
                return repository.value();
            }
        }
        if (clazz.isAnnotationPresent(Controller.class)) {
            Controller controller = clazz.getAnnotation(Controller.class);
            if (!controller.value().isEmpty()) {
                return controller.value();
            }
        }
        
        // 🔥 새로 추가: @Configuration 지원
        if (clazz.isAnnotationPresent(Configuration.class)) {
            Configuration configuration = clazz.getAnnotation(Configuration.class);
            if (!configuration.value().isEmpty()) {
                return configuration.value();
            }
        }
        
        // 기본적으로 클래스명의 첫 글자를 소문자로
        String simpleName = clazz.getSimpleName();
        return simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);
    }
    
    private void findAutowiredMembers(BeanDefinition beanDefinition) {
        Class<?> clazz = beanDefinition.getBeanClass();
        
        // @Autowired 필드 찾기
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Autowired.class)) {
                field.setAccessible(true);
                beanDefinition.getAutowiredFields().add(field);
            }
        }
        
        // @Autowired 메소드 찾기
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(Autowired.class)) {
                method.setAccessible(true);
                beanDefinition.getAutowiredMethods().add(method);
            }
        }
        
        // @Autowired 생성자 찾기
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        for (Constructor<?> constructor : constructors) {
            if (constructor.isAnnotationPresent(Autowired.class)) {
                constructor.setAccessible(true);
                beanDefinition.setAutowiredConstructor(constructor);
                break;
            }
        }
    }
    
    private void instantiateBeans() {
        for (BeanDefinition beanDefinition : beanDefinitionMap.values()) {
            if (!beanDefinition.isLazy()) {
                getBean(beanDefinition.getBeanName());
            }
        }
    }
    
    @Override
    public Object getBean(String name) {
        BeanDefinition beanDefinition = beanDefinitionMap.get(name);
        if (beanDefinition == null) {
            throw new RuntimeException("No bean found with name: " + name);
        }
        
        if (beanDefinition.isSingleton()) {
            Object instance = singletonBeans.get(name);
            if (instance == null) {
                instance = createBean(beanDefinition);
                singletonBeans.put(name, instance);
            }
            return instance;
        } else {
            return createBean(beanDefinition);
        }
    }
    
    @Override
    public <T> T getBean(String name, Class<T> requiredType) {
        Object bean = getBean(name);
        if (requiredType.isInstance(bean)) {
            return requiredType.cast(bean);
        }
        throw new RuntimeException("Bean " + name + " is not of required type " + requiredType.getName());
    }
    
    @Override
    public <T> T getBean(Class<T> requiredType) {
        String beanName = typeToNameMap.get(requiredType);
        if (beanName != null) {
            return getBean(beanName, requiredType);
        }
        
        // 타입으로 빈 찾기
        for (BeanDefinition beanDefinition : beanDefinitionMap.values()) {
            if (requiredType.isAssignableFrom(beanDefinition.getBeanClass())) {
                return getBean(beanDefinition.getBeanName(), requiredType);
            }
        }
        
        throw new RuntimeException("No bean found of type: " + requiredType.getName());
    }
    
    @Override
    public boolean containsBean(String name) {
        return beanDefinitionMap.containsKey(name);
    }
    
    @Override
    public boolean isSingleton(String name) {
        BeanDefinition beanDefinition = beanDefinitionMap.get(name);
        return beanDefinition != null && beanDefinition.isSingleton();
    }
    
    @Override
    public Class<?> getType(String name) {
        BeanDefinition beanDefinition = beanDefinitionMap.get(name);
        return beanDefinition != null ? beanDefinition.getBeanClass() : null;
    }
    
    @Override
    public void close() {
        System.out.println("🔴 ApplicationContext 종료 중...");
        
        // 🔥 새로 추가: 빈 소멸 시 @PreDestroy 메서드 호출
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            String beanName = entry.getKey();
            BeanDefinition beanDefinition = entry.getValue();
            Object bean = singletonBeans.get(beanName);
            
            if (bean != null) {
                try {
                    // 1. @PreDestroy 메서드들 호출
                    for (Method preDestroyMethod : beanDefinition.getPreDestroyMethods()) {
                        preDestroyMethod.setAccessible(true);
                        preDestroyMethod.invoke(bean);
                        System.out.println("🛑 @PreDestroy 호출: " + beanName + "." + preDestroyMethod.getName());
                    }
                    
                    // 2. @Bean의 destroyMethod 호출 (있는 경우)
                    String destroyMethodName = beanDefinition.getDestroyMethodName();
                    if (destroyMethodName != null && !destroyMethodName.isEmpty()) {
                        try {
                            Method destroyMethod = bean.getClass().getDeclaredMethod(destroyMethodName);
                            destroyMethod.setAccessible(true);
                            destroyMethod.invoke(bean);
                            System.out.println("🛑 destroyMethod 호출: " + beanName + "." + destroyMethodName);
                        } catch (NoSuchMethodException e) {
                            System.err.println("⚠️  destroyMethod 를 찾을 수 없습니다: " + destroyMethodName);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("❌ 빈 소멸 중 오류 발생: " + beanName);
                    e.printStackTrace();
                }
            }
        }
        
        running = false;
        singletonBeans.clear();
        System.out.println("✅ ApplicationContext 종료 완료");
    }
    
    @Override
    public boolean isRunning() {
        return running;
    }
    
    @Override
    public String[] getBeanDefinitionNames() {
        return beanDefinitionMap.keySet().toArray(new String[0]);
    }
    
    private Object createBean(BeanDefinition beanDefinition) {
        String beanName = beanDefinition.getBeanName();
        
        // 순환 의존성 체크
        if (creatingBeans.contains(beanName)) {
            throw new RuntimeException("Circular dependency detected for bean: " + beanName);
        }
        
        creatingBeans.add(beanName);
        
        try {
            Object instance;
            
            // 🔥 @Bean 메서드로 생성된 빈인지 확인
            if (beanDefinition.isBeanMethod()) {
                instance = createBeanFromMethod(beanDefinition);
            } else {
                instance = instantiateBean(beanDefinition);
                populateBean(instance, beanDefinition);
            }
            
            // 빈 초기화 (라이프사이클 메서드 호출)
            initializeBean(instance, beanDefinition);
            
            // 🔥 AOP 프록시 적용 (Aspect 클래스가 아닌 경우만)
            if (!beanDefinition.getBeanClass().isAnnotationPresent(Aspect.class)) {
                if (aopProxyFactory.needsProxy(instance)) {
                    Object aopProxy = aopProxyFactory.createProxy(instance);
                    System.out.println("🎭 AOP 프록시 생성: " + beanName + " → " + aopProxy.getClass().getSimpleName());
                    instance = aopProxy;
                }
            }
            
            // @Transactional이 있으면 트랜잭션 프록시 생성
            if (needsProxy(beanDefinition.getBeanClass())) {
                Object txProxy = proxyFactory.createProxy(instance);
                System.out.println("💳 트랜잭션 프록시 생성: " + beanName);
                instance = txProxy;
            }
            
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create bean: " + beanName, e);
        } finally {
            creatingBeans.remove(beanName);
        }
    }
    
    /**
     * 🔥 새로 추가: @Bean 메서드로부터 빈 생성
     */
    private Object createBeanFromMethod(BeanDefinition beanDefinition) throws Exception {
        Method beanMethod = beanDefinition.getBeanMethod();
        Object configInstance = beanDefinition.getConfigurationInstance();
        
        // 메서드 파라미터들에 대한 의존성 주입
        Parameter[] parameters = beanMethod.getParameters();
        Object[] args = new Object[parameters.length];
        
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            
            // 파라미터에 대한 의존성 주입
            Object dependency = getBean(parameter.getType());
            args[i] = dependency;
        }
        
        // @Bean 메서드 호출
        beanMethod.setAccessible(true);
        Object bean = beanMethod.invoke(configInstance, args);
        
        System.out.println("🔧 @Bean 메서드로 빈 생성: " + beanDefinition.getBeanName() + " = " + bean);
        return bean;
    }
    
    /**
     * 🔥 새로 추가: 빈 초기화 및 라이프사이클 메서드 호출
     */
    private void initializeBean(Object bean, BeanDefinition beanDefinition) throws Exception {
        // 1. @PostConstruct 메서드들 호출
        for (Method postConstructMethod : beanDefinition.getPostConstructMethods()) {
            postConstructMethod.setAccessible(true);
            postConstructMethod.invoke(bean);
            System.out.println("🚀 @PostConstruct 호출: " + beanDefinition.getBeanName() + "." + postConstructMethod.getName());
        }
        
        // 2. @Bean의 initMethod 호출 (있는 경우)
        String initMethodName = beanDefinition.getInitMethodName();
        if (initMethodName != null && !initMethodName.isEmpty()) {
            try {
                Method initMethod = bean.getClass().getDeclaredMethod(initMethodName);
                initMethod.setAccessible(true);
                initMethod.invoke(bean);
                System.out.println("🚀 initMethod 호출: " + beanDefinition.getBeanName() + "." + initMethodName);
            } catch (NoSuchMethodException e) {
                System.err.println("⚠️  initMethod 를 찾을 수 없습니다: " + initMethodName);
            }
        }
    }
    
    private Object instantiateBean(BeanDefinition beanDefinition) {
        try {
            Class<?> beanClass = beanDefinition.getBeanClass();
            Constructor<?> autowiredConstructor = beanDefinition.getAutowiredConstructor();
            
            if (autowiredConstructor != null) {
                // @Autowired 생성자 사용
                Class<?>[] paramTypes = autowiredConstructor.getParameterTypes();
                Object[] args = new Object[paramTypes.length];
                
                for (int i = 0; i < paramTypes.length; i++) {
                    args[i] = getBean(paramTypes[i]);
                }
                
                return autowiredConstructor.newInstance(args);
            } else {
                // 기본 생성자 사용
                return beanClass.getDeclaredConstructor().newInstance();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate bean: " + beanDefinition.getBeanName(), e);
        }
    }
    
    private void populateBean(Object instance, BeanDefinition beanDefinition) {
        // 필드 주입
        for (Field field : beanDefinition.getAutowiredFields()) {
            try {
                Object dependency = getBean(field.getType());
                field.set(instance, dependency);
            } catch (Exception e) {
                throw new RuntimeException("Failed to inject field: " + field.getName(), e);
            }
        }
        
        // 메소드 주입
        for (Method method : beanDefinition.getAutowiredMethods()) {
            try {
                Class<?>[] paramTypes = method.getParameterTypes();
                Object[] args = new Object[paramTypes.length];
                
                for (int i = 0; i < paramTypes.length; i++) {
                    args[i] = getBean(paramTypes[i]);
                }
                
                method.invoke(instance, args);
            } catch (Exception e) {
                throw new RuntimeException("Failed to inject method: " + method.getName(), e);
            }
        }
    }
    
    private boolean needsProxy(Class<?> clazz) {
        // 클래스나 메소드에 @Transactional이 있는지 확인
        if (clazz.isAnnotationPresent(Transactional.class)) {
            return true;
        }
        
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Transactional.class)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 🔥 새로 추가: @PostConstruct, @PreDestroy 메서드 스캔
     */
    private void scanLifecycleMethods(Class<?> clazz, BeanDefinition beanDefinition) {
        Method[] methods = clazz.getDeclaredMethods();
        
        for (Method method : methods) {
            if (method.isAnnotationPresent(PostConstruct.class)) {
                beanDefinition.getPostConstructMethods().add(method);
                System.out.println("  📋 @PostConstruct 메서드 발견: " + method.getName());
            }
            
            if (method.isAnnotationPresent(PreDestroy.class)) {
                beanDefinition.getPreDestroyMethods().add(method);
                System.out.println("  📋 @PreDestroy 메서드 발견: " + method.getName());
            }
        }
    }
    
    /**
     * 🔥 새로 추가: @Configuration 클래스들을 먼저 인스턴스화
     */
    private void instantiateConfigurationClasses() {
        for (BeanDefinition bd : beanDefinitionMap.values()) {
            if (bd.getBeanClass().isAnnotationPresent(Configuration.class)) {
                try {
                    Object configInstance = createBean(bd);
                    configurationInstances.put(bd.getBeanClass(), configInstance);
                    System.out.println("⚙️  Configuration 클래스 인스턴스화: " + bd.getBeanName());
                } catch (Exception e) {
                    System.err.println("❌ Configuration 클래스 인스턴스화 실패: " + bd.getBeanName());
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * 🔥 새로 추가: @Configuration 클래스들에서 @Bean 메서드 스캔
     */
    private void scanBeanMethods() {
        for (Map.Entry<Class<?>, Object> entry : configurationInstances.entrySet()) {
            Class<?> configClass = entry.getKey();
            Object configInstance = entry.getValue();
            
            Method[] methods = configClass.getDeclaredMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(Bean.class)) {
                    createBeanDefinitionFromMethod(method, configInstance);
                }
            }
        }
    }
    
    /**
     * 🔥 새로 추가: @Bean 메서드로부터 BeanDefinition 생성
     */
    private void createBeanDefinitionFromMethod(Method method, Object configInstance) {
        Bean beanAnnotation = method.getAnnotation(Bean.class);
        
        // 빈 이름 결정 (value가 있으면 사용, 없으면 메서드명 사용)
        String beanName;
        if (beanAnnotation.value().length > 0 && !beanAnnotation.value()[0].isEmpty()) {
            beanName = beanAnnotation.value()[0];
        } else {
            beanName = method.getName();
        }
        
        // 반환 타입을 빈 클래스로 사용
        Class<?> beanClass = method.getReturnType();
        
        BeanDefinition beanDefinition = new BeanDefinition(beanName, beanClass, method, configInstance);
        
        // @Bean 어노테이션 속성들 설정
        beanDefinition.setInitMethodName(beanAnnotation.initMethod());
        beanDefinition.setDestroyMethodName(beanAnnotation.destroyMethod());
        beanDefinition.setDefaultCandidate(beanAnnotation.defaultCandidate());
        
        beanDefinitionMap.put(beanName, beanDefinition);
        typeToNameMap.put(beanClass, beanName);
        System.out.println("🔧 @Bean 메서드로부터 BeanDefinition 등록: " + beanName + " (" + beanClass.getSimpleName() + ")");
    }
    
    /**
     * 🔥 새로 추가: @Aspect 애스펙트 스캔 및 등록
     */
    private void scanAndRegisterAspects() {
        for (BeanDefinition bd : beanDefinitionMap.values()) {
            if (bd.getBeanClass().isAnnotationPresent(Aspect.class)) {
                try {
                    // Aspect 인스턴스 생성
                    Object aspectInstance = getBean(bd.getBeanName());
                    
                    // AspectScanner를 사용해서 aspect 메타데이터 처리
                    AspectMetadata aspectMetadata = AspectScanner.processAspect(aspectInstance);
                    aspects.add(aspectMetadata);
                    
                    // AopProxyFactory에 aspect 추가
                    aopProxyFactory.addAspect(aspectMetadata);
                    
                    System.out.println("✅ Aspect 등록 완료: " + bd.getBeanName());
                } catch (Exception e) {
                    System.err.println("❌ Aspect 등록 실패: " + bd.getBeanName());
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * 수동으로 싱글톤 빈을 등록합니다 (트랜잭션 관련 빈들을 위해 추가)
     */
    public void registerBean(String beanName, Object beanInstance) {
        singletonBeans.put(beanName, beanInstance);
        typeToNameMap.put(beanInstance.getClass(), beanName);
        
        // 간단한 BeanDefinition도 생성해서 등록
        BeanDefinition beanDefinition = new BeanDefinition(beanName, beanInstance.getClass());
        beanDefinitionMap.put(beanName, beanDefinition);
        
        System.out.println("✅ 수동 빈 등록: " + beanName + " (" + beanInstance.getClass().getSimpleName() + ")");
    }
} 