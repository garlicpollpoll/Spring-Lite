package com.springlite.demo.controller;

import com.springlite.demo.model.User;
import com.springlite.demo.service.UserService;
import com.springlite.framework.annotations.Autowired;
import com.springlite.framework.annotations.Controller;
import com.springlite.framework.annotations.GetMapping;
import com.springlite.framework.annotations.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("")
    public List<User> getAllUsers() {
        System.out.println("UserController: GET /api/users");
        return userService.getAllUsers();
    }
    
    @GetMapping("/hello")
    public String hello() {
        System.out.println("UserController: GET /api/users/hello");
        return "{\"message\":\"Hello from Spring Lite!\"}";
    }
    
    @GetMapping("/test")
    public User getTestUser() {
        System.out.println("UserController: GET /api/users/test");
        return new User(999L, "테스트 사용자", "test@example.com");
    }
} 