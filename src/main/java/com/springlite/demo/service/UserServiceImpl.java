package com.springlite.demo.service;

import com.springlite.demo.dto.User;
import com.springlite.framework.annotations.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 🔄 AOP 테스트를 위한 UserService 구현체
 */
@Service
public class UserServiceImpl implements UserService {
    
    private final Map<Long, User> users = new HashMap<>();
    private Long nextId = 1L;
    
    @Override
    public User getUserById(Long id) {
        System.out.println("💾 데이터베이스에서 사용자 조회: " + id);
        
        // 실행 시간을 위해 잠시 대기
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        User user = users.get(id);
        if (user == null) {
            throw new RuntimeException("사용자를 찾을 수 없습니다: " + id);
        }
        return user;
    }
    
    @Override
    public User createUser(String name, String email) {
        System.out.println("💾 새 사용자 생성: " + name + " (" + email + ")");
        
        User user = new User(nextId++, name, email);
        users.put(user.getId(), user);
        
        return user;
    }
    
    @Override
    public User updateUser(Long id, String name, String email) {
        System.out.println("💾 사용자 정보 업데이트: " + id);
        
        User existingUser = users.get(id);
        if (existingUser == null) {
            throw new RuntimeException("사용자를 찾을 수 없습니다: " + id);
        }
        
        User updatedUser = new User(id, name, email);
        users.put(id, updatedUser);
        
        return updatedUser;
    }
    
    @Override
    public void deleteUser(Long id) {
        System.out.println("💾 사용자 삭제: " + id);
        
        if (!users.containsKey(id)) {
            throw new RuntimeException("사용자를 찾을 수 없습니다: " + id);
        }
        
        users.remove(id);
    }
    
    @Override
    public void throwException(String message) {
        System.out.println("💾 예외 발생 테스트");
        throw new RuntimeException("테스트 예외: " + message);
    }
    
    @Override
    public List<User> getAllUsers() {
        System.out.println("💾 모든 사용자 조회");
        return new ArrayList<>(users.values());
    }
} 