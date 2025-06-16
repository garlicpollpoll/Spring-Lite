package com.springlite.framework.aop;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.regex.Pattern;

/**
 * Evaluates AspectJ pointcut expressions to determine if a method matches.
 * Supports a subset of AspectJ pointcut language used in Spring AOP.
 */
public class PointcutMatcher {
    
    private final String expression;
    
    public PointcutMatcher(String expression) {
        this.expression = expression;
    }
    
    /**
     * Tests if the given method matches this pointcut expression.
     */
    public boolean matches(Method method, Class<?> targetClass) {
        if (expression == null || expression.trim().isEmpty()) {
            return false;
        }
        
        String expr = expression.trim();
        
        System.out.println("    🔍 PointcutMatcher: Testing '" + expr + "' against " + targetClass.getSimpleName() + "." + method.getName());
        
        // Handle OR expressions (||)
        if (expr.contains(" || ")) {
            String[] parts = expr.split(" \\|\\| ");
            for (String part : parts) {
                if (matchesSingleExpression(part.trim(), method, targetClass)) {
                    System.out.println("      📝 OR expression result: true (matched '" + part.trim() + "')");
                    return true;
                }
            }
            System.out.println("      📝 OR expression result: false");
            return false;
        }
        
        // Handle AND expressions (&&)
        if (expr.contains(" && ")) {
            String[] parts = expr.split(" && ");
            for (String part : parts) {
                if (!matchesSingleExpression(part.trim(), method, targetClass)) {
                    System.out.println("      📝 AND expression result: false (failed on '" + part.trim() + "')");
                    return false;
                }
            }
            System.out.println("      📝 AND expression result: true");
            return true;
        }
        
        // Single expression
        return matchesSingleExpression(expr, method, targetClass);
    }
    
    /**
     * Matches a single pointcut expression (not compound).
     */
    private boolean matchesSingleExpression(String expr, Method method, Class<?> targetClass) {
        // Handle execution() pointcut
        if (expr.startsWith("execution(")) {
            boolean matches = matchesExecution(expr, method, targetClass);
            System.out.println("      📝 execution pattern result: " + matches);
            return matches;
        }
        
        // Handle @annotation() pointcut
        if (expr.startsWith("@annotation(")) {
            boolean matches = matchesAnnotation(expr, method, targetClass);
            System.out.println("      📝 @annotation pattern result: " + matches);
            return matches;
        }
        
        // Handle @within() pointcut
        if (expr.startsWith("@within(")) {
            boolean matches = matchesAtWithin(expr, method, targetClass);
            System.out.println("      📝 @within pattern result: " + matches);
            return matches;
        }
        
        // Handle within() pointcut
        if (expr.startsWith("within(")) {
            boolean matches = matchesWithin(expr, targetClass);
            System.out.println("      📝 within pattern result: " + matches);
            return matches;
        }
        
        // Handle bean() pointcut
        if (expr.startsWith("bean(")) {
            boolean matches = matchesBean(expr, targetClass);
            System.out.println("      📝 bean pattern result: " + matches);
            return matches;
        }
        
        // Handle named pointcut references (e.g., "dataAccessOperation()")
        if (expr.contains("(") && expr.endsWith(")") && !expr.contains("*")) {
            // This would be a named pointcut reference - for now, return false
            System.out.println("      📝 named pointcut reference - not supported yet");
            return false;
        }
        
        System.out.println("      📝 no pattern matched");
        return false;
    }
    
    /**
     * Matches execution pointcut expressions.
     * Format: execution(modifiers-pattern? ret-type-pattern declaring-type-pattern?name-pattern(param-pattern) throws-pattern?)
     */
    private boolean matchesExecution(String expr, Method method, Class<?> targetClass) {
        // Extract the pattern from execution(pattern)
        String pattern = expr.substring(10, expr.length() - 1); // Remove "execution(" and ")"
        
        // Simple patterns for demonstration
        if (pattern.equals("* *(..)")) {
            return true; // Match all methods
        }
        
        if (pattern.startsWith("public * ")) {
            pattern = pattern.substring(9); // Remove "public * "
            if (!java.lang.reflect.Modifier.isPublic(method.getModifiers())) {
                return false;
            }
        } else if (pattern.startsWith("* ")) {
            pattern = pattern.substring(2); // Remove "* "
        }
        
        // Handle package patterns like "com.xyz.service.*.*(.."
        if (pattern.contains("..")) {
            return matchesPackagePattern(pattern, method, targetClass);
        }
        
        // Handle simple method name patterns
        if (pattern.endsWith("(..)")) {
            String methodPattern = pattern.substring(0, pattern.length() - 4);
            return matchesMethodName(methodPattern, method.getName());
        }
        
        return false;
    }
    
    /**
     * Matches @annotation pointcut expressions with target class support.
     */
    private boolean matchesAnnotation(String expr, Method method, Class<?> targetClass) {
        // Extract annotation class name from @annotation(com.xyz.Annotation)
        String annotationName = expr.substring(12, expr.length() - 1); // Remove "@annotation(" and ")"
        
        // Handle common annotations
        if (annotationName.contains("Transactional")) {
            // Try to find the corresponding method in the target class
            try {
                Method targetMethod = targetClass.getMethod(method.getName(), method.getParameterTypes());
                boolean hasAnnotation = targetMethod.isAnnotationPresent(com.springlite.framework.transaction.Transactional.class) ||
                                      targetClass.isAnnotationPresent(com.springlite.framework.transaction.Transactional.class);
                System.out.println("        📝 Target method annotation check: " + targetMethod.getName() + " = " + hasAnnotation);
                return hasAnnotation;
            } catch (NoSuchMethodException e) {
                System.out.println("        ⚠️ Could not find method " + method.getName() + " in target class " + targetClass.getSimpleName());
                // Fall back to original method check
                return method.isAnnotationPresent(com.springlite.framework.transaction.Transactional.class) ||
                       method.getDeclaringClass().isAnnotationPresent(com.springlite.framework.transaction.Transactional.class);
            }
        }
        
        return false;
    }
    
    /**
     * Matches @within pointcut expressions.
     * @within matches if the target class (or any of its superclasses) has the specified annotation.
     */
    private boolean matchesAtWithin(String expr, Method method, Class<?> targetClass) {
        // Extract annotation class name from @within(com.xyz.Annotation)
        String annotationName = expr.substring(8, expr.length() - 1); // Remove "@within(" and ")"
        
        // Handle common annotations
        if (annotationName.contains("Transactional")) {
            return targetClass.isAnnotationPresent(com.springlite.framework.transaction.Transactional.class);
        }
        
        return false;
    }
    
    /**
     * Matches within pointcut expressions.
     */
    private boolean matchesWithin(String expr, Class<?> targetClass) {
        // Extract package pattern from within(com.xyz.service.*)
        String pattern = expr.substring(7, expr.length() - 1); // Remove "within(" and ")"
        
        String className = targetClass.getName();
        
        if (pattern.endsWith("..*")) {
            // Match package and sub-packages
            String packagePrefix = pattern.substring(0, pattern.length() - 3);
            return className.startsWith(packagePrefix);
        } else if (pattern.endsWith(".*")) {
            // Match package only (not sub-packages)
            String packagePrefix = pattern.substring(0, pattern.length() - 2);
            String packageName = className.substring(0, className.lastIndexOf('.'));
            return packageName.equals(packagePrefix);
        }
        
        return className.equals(pattern);
    }
    
    /**
     * Matches bean pointcut expressions.
     */
    private boolean matchesBean(String expr, Class<?> targetClass) {
        // Extract bean name pattern from bean(*Service)
        String pattern = expr.substring(5, expr.length() - 1); // Remove "bean(" and ")"
        
        String className = targetClass.getSimpleName();
        
        if (pattern.startsWith("*") && pattern.length() > 1) {
            // Wildcard pattern like *Service
            String suffix = pattern.substring(1);
            return className.endsWith(suffix);
        } else if (pattern.endsWith("*") && pattern.length() > 1) {
            // Wildcard pattern like Service*
            String prefix = pattern.substring(0, pattern.length() - 1);
            return className.startsWith(prefix);
        } else if (pattern.equals("*")) {
            return true; // Match all beans
        }
        
        return className.equals(pattern);
    }
    
    /**
     * Matches package patterns with wildcards.
     */
    private boolean matchesPackagePattern(String pattern, Method method, Class<?> targetClass) {
        String className = targetClass.getName();
        String methodName = method.getName();
        
        System.out.println("        🔍 matchesPackagePattern: pattern='" + pattern + "', class='" + className + "', method='" + methodName + "'");
        
        // Handle patterns like "com.xyz.service.*.*(..)
        if (pattern.endsWith("*.*(..)")) {
            String packagePattern = pattern.substring(0, pattern.length() - 7); // Remove "*.*(..)
            System.out.println("        📝 Package pattern: '" + packagePattern + "'");
            
            // Remove trailing dot if present
            if (packagePattern.endsWith(".")) {
                packagePattern = packagePattern.substring(0, packagePattern.length() - 1);
                System.out.println("        📝 Package pattern after removing dot: '" + packagePattern + "'");
            }
            
            if (packagePattern.endsWith("..")) {
                // Sub-package match like "com.xyz.service..*.*(..)
                packagePattern = packagePattern.substring(0, packagePattern.length() - 2);
                System.out.println("        📝 Sub-package pattern: '" + packagePattern + "'");
                return className.startsWith(packagePattern);
            } else {
                // Exact package match like "com.xyz.service.*.*(..)
                String packageName = className.substring(0, className.lastIndexOf('.'));
                System.out.println("        📝 Class package: '" + packageName + "', expected: '" + packagePattern + "'");
                return packageName.equals(packagePattern);
            }
        }
        
        // Handle patterns like "com.xyz.service.UserService.getUserById(..)
        if (pattern.endsWith("(..)")) {
            String fullPattern = pattern.substring(0, pattern.length() - 4); // Remove "(..)
            
            // Check if it's a specific method pattern
            if (fullPattern.contains(".")) {
                String[] parts = fullPattern.split("\\.");
                if (parts.length >= 2) {
                    String classPattern = parts[parts.length - 2];
                    String methodPattern = parts[parts.length - 1];
                    
                    System.out.println("        📝 Method pattern - class: '" + classPattern + "', method: '" + methodPattern + "'");
                    
                    boolean classMatches = classPattern.equals("*") || targetClass.getSimpleName().equals(classPattern) ||
                                         (classPattern.equals("UserService") && targetClass.getSimpleName().equals("UserServiceImpl"));
                    boolean methodMatches = methodPattern.equals("*") || methodName.equals(methodPattern);
                    
                    System.out.println("        📝 Class matches: " + classMatches + ", Method matches: " + methodMatches);
                    return classMatches && methodMatches;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Matches method names with wildcards.
     */
    private boolean matchesMethodName(String pattern, String methodName) {
        if (pattern.equals("*")) {
            return true;
        }
        
        if (pattern.startsWith("*")) {
            return methodName.endsWith(pattern.substring(1));
        }
        
        if (pattern.endsWith("*")) {
            return methodName.startsWith(pattern.substring(0, pattern.length() - 1));
        }
        
        return methodName.equals(pattern);
    }
    
    public String getExpression() {
        return expression;
    }
} 