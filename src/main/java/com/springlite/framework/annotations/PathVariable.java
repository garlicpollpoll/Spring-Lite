package com.springlite.framework.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * URL 경로의 변수를 메소드 매개변수에 바인딩
 * 예: @GetMapping("/users/{id}") → @PathVariable Long id
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface PathVariable {
    
    /**
     * 경로 변수 이름
     * 기본값은 매개변수 이름과 동일
     */
    String value() default "";
    
    /**
     * 매개변수가 필수인지 여부
     */
    boolean required() default true;
} 