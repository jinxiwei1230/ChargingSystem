package com.online.chargingSystem.controller;

import com.online.chargingSystem.common.Result;
import com.online.chargingSystem.entity.User;
import com.online.chargingSystem.entity.ChargingRequest;
import com.online.chargingSystem.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户控制器
 * 处理用户相关的请求，包括登录、注册等
 */
@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "http://localhost:8081")
public class UserController {
    @Resource
    private UserService userService;

    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return 登录结果
     */
    @RequestMapping("/login")
    public Result<User> loginController(@RequestParam String username,
                                      @RequestParam String password) {
        User user = userService.loginService(username, password);
        if (user != null) {
            return Result.success("登录成功！", user);
        } else {
            return Result.error("账号或密码错误！");
        }
    }

    /**
     * 用户注册
     * @param username 用户名
     * @param password 密码
     * @param carNumber 车牌号
     * @return 注册结果
     */
    @RequestMapping("/register")
    public Result<User> registController(@RequestParam String username,
                                       @RequestParam String password,
                                       @RequestParam String carNumber){
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(password);
        newUser.setCarNumber(carNumber);

        User user = userService.registService(newUser);
        if(user!=null){
            return Result.success("注册成功！",user);
        }else{
            return Result.error("用户名或车牌号已存在！");
        }
    }

    /**
     * 获取用户信息
     * @param userId 用户ID
     * @return 用户信息
     */
    @GetMapping("/info")
    public Result<User> getUserInfo(@RequestParam Long userId) {
        User user = userService.getUserInfo(userId);
        if (user != null) {
            return Result.success("获取用户信息成功", user);
        } else {
            return Result.error("用户不存在");
        }
    }

    /**
     * 获取用户充电请求列表
     * @param userId 用户ID
     * @return 充电请求列表
     */
    @GetMapping("/requests")
    public Result<List<ChargingRequest>> getUserChargingRequests(@RequestParam Long userId) {
        List<ChargingRequest> requests = userService.getUserChargingRequests(userId);
        if (requests == null || requests.isEmpty()) {
            return Result.error("该用户暂无充电请求记录");
        }
        return Result.success("获取用户充电请求列表成功", requests);
    }
}

