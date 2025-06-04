package com.springlite.framework.context;

import com.springlite.framework.annotations.*;
import com.springlite.framework.beans.BeanDefinition;
import com.springlite.framework.proxy.ProxyFactory;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AnnotationApplicationContext implements ApplicationContext {
    
    private Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    private Map<String, Object> singletonBeans = new ConcurrentHashMap<>();
    private Map<Class<?>, String> typeToNameMap = new ConcurrentHashMap<>();
    private Set<String> creatingBeans = new HashSet<>();
    private boolean running = false;
    private ProxyFactory proxyFactory;
    
    public AnnotationApplicationContext(Class<?> configClass) {
        this.proxyFactory = new ProxyFactory();
        scan(configClass);
        refresh();
    }
    
    public AnnotationApplicationContext(String... basePackages) {
        this.proxyFactory = new ProxyFactory();
        scanPackages(basePackages);
        refresh();
    }
    
    @Override
    public void refresh() {
        // 1. 빈 정의 스캔 (이미 완료)
        // 2. 빈 인스턴스 생성 및 의존성 주입
        instantiateBeans();
        // 3. 애플리케이션 컨텍스트 시작
        running = true;
        System.out.println("ApplicationContext refreshed with " + beanDefinitionMap.size() + " beans");
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
    
    private void scanPackages(String... basePackages) {
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
               clazz.isAnnotationPresent(Controller.class);
    }
    
    private void registerBean(Class<?> clazz) {
        String beanName = getBeanName(clazz);
        BeanDefinition beanDefinition = new BeanDefinition(beanName, clazz);
        
        // Autowired 필드, 메소드, 생성자 찾기
        findAutowiredMembers(beanDefinition);
        
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
        running = false;
        singletonBeans.clear();
        System.out.println("ApplicationContext closed");
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
            Object instance = instantiateBean(beanDefinition);
            populateBean(instance, beanDefinition);
            
            // @Transactional이 있으면 프록시 생성
            if (needsProxy(beanDefinition.getBeanClass())) {
                instance = proxyFactory.createProxy(instance);
            }
            
            return instance;
        } finally {
            creatingBeans.remove(beanName);
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
} 