package com.online.chargingSystem.service.impl;

import com.online.chargingSystem.entity.User;
import com.online.chargingSystem.mapper.UserMapper;
import com.online.chargingSystem.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Resource
    private UserMapper userMapper;

    @Override
    public User loginService(String uname, String password) {
        return userMapper.selectByUsernameAndPassword(uname, password);
    }

    @Override
    public User registService(User user) {
        if (userMapper.selectByUsername(user.getUsername()) != null) {
            return null;
        }
        userMapper.insert(user);
        return user;
    }
}
