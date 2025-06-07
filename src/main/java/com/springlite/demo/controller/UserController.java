package com.springlite.demo.controller;

import com.springlite.demo.model.User;
import com.springlite.demo.service.UserService;
import com.springlite.framework.annotations.Autowired;
import com.springlite.framework.annotations.Controller;
import com.springlite.framework.annotations.GetMapping;
import com.springlite.framework.annotations.RequestMapping;
import com.springlite.framework.web.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("")
    public List<User> getAllUsers() {
        System.out.println("UserController: GET /api/users (REST API)");
        return userService.getAllUsers();
    }
    
    @GetMapping("/hello")
    public String hello() {
        System.out.println("UserController: GET /api/users/hello (REST API)");
        return "{\"message\":\"Hello from Spring Lite!\"}";
    }
    
    @GetMapping("/test")
    public User getTestUser() {
        System.out.println("UserController: GET /api/users/test (REST API)");
        return new User(999L, "테스트 사용자", "test@example.com");
    }
    
    @GetMapping("/view")
    public ModelAndView getUsersView() {
        System.out.println("UserController: GET /api/users/view (MVC Pattern)");
        
        List<User> users = userService.getAllUsers();
        
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("user/list");
        modelAndView.addObject("users", users);
        modelAndView.addObject("title", "사용자 목록");
        modelAndView.addObject("totalCount", users.size());
        
        return modelAndView;
    }
    
    @GetMapping("/detail")
    public ModelAndView getUserDetail() {
        System.out.println("UserController: GET /api/users/detail (MVC Pattern)");
        
        // 예시로 첫 번째 사용자 상세 정보를 보여줌
        List<User> users = userService.getAllUsers();
        User user = users.isEmpty() ? null : users.get(0);
        
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("user/detail");
        modelAndView.addObject("user", user);
        
        return modelAndView;
    }
} 