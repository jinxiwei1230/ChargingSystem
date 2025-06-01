package com.online.chargingSystem.service.impl;

import com.online.chargingSystem.entity.User;
import com.online.chargingSystem.mapper.UserMapper;
import com.online.chargingSystem.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.util.Random;

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
        if (userMapper.selectByUsername(user.getUsername()) != null ||
                userMapper.selectByCarNumber(user.getCarNumber()) != null) {
            return null;
        }
        Random random = new Random();
        double batteryCapacity = (random.nextInt(5) + 5) * 10;
        user.setBatteryCapacity(batteryCapacity);
        userMapper.insert(user);
        return user;
    }
}
