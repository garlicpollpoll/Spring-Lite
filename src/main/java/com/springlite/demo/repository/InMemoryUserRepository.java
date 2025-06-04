package com.springlite.demo.repository;

import com.springlite.demo.model.User;
import com.springlite.framework.annotations.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryUserRepository implements UserRepository {
    
    private final Map<Long, User> users = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    public InMemoryUserRepository() {
        // 초기 데이터
        save(new User(null, "김철수", "kim@example.com"));
        save(new User(null, "이영희", "lee@example.com"));
        save(new User(null, "박민수", "park@example.com"));
    }
    
    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }
    
    @Override
    public User findById(Long id) {
        return users.get(id);
    }
    
    @Override
    public User save(User user) {
        if (user.getId() == null) {
            user.setId(idGenerator.getAndIncrement());
        }
        users.put(user.getId(), user);
        return user;
    }
    
    @Override
    public void deleteById(Long id) {
        users.remove(id);
    }
} 