package com.springlite.framework.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * HTTP 요청 본문을 메소드 매개변수에 바인딩
 * JSON, XML 등의 데이터를 객체로 변환
 * 예: POST /users + JSON body → @RequestBody User user
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestBody {
    
    /**
     * 매개변수가 필수인지 여부
     */
    boolean required() default true;
} 