package com.springlite.demo.service;

import com.springlite.demo.dto.User;

import java.util.List;

/**
 * 🔄 AOP 테스트를 위한 UserService 인터페이스
 */
public interface UserService {
    
    /**
     * 모든 사용자 조회
     */
    List<User> getAllUsers();
    
    /**
     * 사용자 조회
     */
    User getUserById(Long id);
    
    /**
     * 사용자 생성
     */
    User createUser(String name, String email);
    
    /**
     * 사용자 업데이트
     */
    User updateUser(Long id, String name, String email);
    
    /**
     * 사용자 삭제
     */
    void deleteUser(Long id);
    
    /**
     * 예외 발생 테스트 메서드
     */
    void throwException(String message);
} 