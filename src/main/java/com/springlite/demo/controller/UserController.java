package com.springlite.demo.controller;

import com.springlite.demo.model.User;
import com.springlite.demo.service.UserService;
import com.springlite.framework.annotations.Autowired;
import com.springlite.framework.annotations.Controller;
import com.springlite.framework.annotations.GetMapping;
import com.springlite.framework.annotations.PathVariable;
import com.springlite.framework.annotations.PostMapping;
import com.springlite.framework.annotations.RequestBody;
import com.springlite.framework.annotations.RequestMapping;
import com.springlite.framework.annotations.RequestParam;
import com.springlite.framework.web.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("")
    public List<User> getAllUsers() {
        System.out.println("UserController: GET /users (REST API)");
        return userService.getAllUsers();
    }
    
    @GetMapping("/hello")
    public String hello() {
        System.out.println("UserController: GET /users/hello (REST API)");
        return "{\"message\":\"Hello from Spring Lite!\"}";
    }
    
    @GetMapping("/test")
    public User getTestUser() {
        System.out.println("UserController: GET /users/test (REST API)");
        return new User(999L, "테스트 사용자", "test@example.com");
    }
    
    @GetMapping("/view")
    public ModelAndView getUsersView() {
        System.out.println("UserController: GET /users/view (MVC Pattern)");
        
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
        System.out.println("UserController: GET /users/detail (MVC Pattern)");
        
        // 예시로 첫 번째 사용자 상세 정보를 보여줌
        List<User> users = userService.getAllUsers();
        User firstUser = users.isEmpty() ? new User("No User", "no@example.com") : users.get(0);
        
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("user/detail");
        modelAndView.addObject("user", firstUser);
        modelAndView.addObject("title", "사용자 상세 정보");
        
        return modelAndView;
    }
    
    // 🔥 새로 추가: @PathVariable 테스트
    @GetMapping("/pathvar/{id}")
    public User getUserById(@PathVariable Long id) {
        System.out.println("UserController: GET /users/pathvar/" + id + " (PathVariable Test)");
        
        // 간단한 예시 응답
        return new User("User " + id, "user" + id + "@example.com");
    }
    
    // 🔥 새로 추가: @RequestParam 테스트  
    @GetMapping("/search")
    public List<User> searchUsers(@RequestParam String name, 
                                 @RequestParam(required = false, defaultValue = "0") Integer page) {
        System.out.println("UserController: GET /users/search?name=" + name + "&page=" + page);
        
        // 간단한 검색 시뮬레이션
        return List.of(new User(name + " (search result)", name.toLowerCase() + "@example.com"));
    }
    
    // 🔥 새로 추가: @RequestBody 테스트
    @PostMapping("/create")
    public User createUser(@RequestBody User user) {
        System.out.println("UserController: POST /users/create with body: " + user.getName());
        
        // 사용자 생성 시뮬레이션
        User createdUser = new User(user.getName(), user.getEmail());
        System.out.println("Created user: " + createdUser.getName() + " <" + createdUser.getEmail() + ">");
        
        return createdUser;
    }
} 