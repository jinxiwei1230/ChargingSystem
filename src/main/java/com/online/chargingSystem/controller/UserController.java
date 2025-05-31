package com.online.chargingSystem.controller;

import com.online.chargingSystem.common.Result;
import com.online.chargingSystem.entity.User;
import com.online.chargingSystem.service.UserService;
import jakarta.annotation.Resource;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;

    @RequestMapping("/login")
    public Result<User> loginController(@RequestBody User request) {
        String username = request.getUsername();
        String password = request.getPassword();
        User user = userService.loginService(username, password);
        if (user != null) {
            return Result.success("登录成功！", user);
        } else {
            return Result.error("账号或密码错误！");
        }
    }

    @RequestMapping("/register")
    public Result<User> registController(@RequestBody User newUser){
        User user = userService.registService(newUser);
        if(user!=null){
            return Result.success("注册成功！",user);
        }else{
            return Result.error("用户名已存在！");
        }
    }
}

