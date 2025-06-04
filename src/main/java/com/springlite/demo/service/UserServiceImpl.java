package com.springlite.demo.service;

import com.springlite.demo.model.User;
import com.springlite.demo.repository.UserRepository;
import com.springlite.framework.annotations.Autowired;
import com.springlite.framework.annotations.Service;
import com.springlite.framework.annotations.Transactional;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        System.out.println("UserService: Getting all users");
        return userRepository.findAll();
    }
    
    @Override
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        System.out.println("UserService: Getting user by id: " + id);
        return userRepository.findById(id);
    }
    
    @Override
    @Transactional
    public User createUser(User user) {
        System.out.println("UserService: Creating user: " + user.getName());
        return userRepository.save(user);
    }
    
    @Override
    @Transactional
    public User updateUser(Long id, User user) {
        System.out.println("UserService: Updating user: " + id);
        User existingUser = userRepository.findById(id);
        if (existingUser != null) {
            existingUser.setName(user.getName());
            existingUser.setEmail(user.getEmail());
            return userRepository.save(existingUser);
        }
        return null;
    }
    
    @Override
    @Transactional
    public void deleteUser(Long id) {
        System.out.println("UserService: Deleting user: " + id);
        userRepository.deleteById(id);
    }
} 