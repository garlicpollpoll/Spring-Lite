package com.springlite.framework.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * HTTP 요청 파라미터를 메소드 매개변수에 바인딩
 * 예: /users?name=john → @RequestParam String name
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestParam {
    
    /**
     * 요청 파라미터 이름
     * 기본값은 매개변수 이름과 동일
     */
    String value() default "";
    
    /**
     * 매개변수가 필수인지 여부
     */
    boolean required() default true;
    
    /**
     * 파라미터가 없을 때 사용할 기본값
     */
    String defaultValue() default "";
} 