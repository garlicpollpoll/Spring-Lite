package com.springlite.demo.repository;

import com.springlite.demo.model.User;
import java.util.List;

public interface UserRepository {
    List<User> findAll();
    User findById(Long id);
    User save(User user);
    void deleteById(Long id);
} 