package com.online.chargingSystem.mapper;

import com.online.chargingSystem.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface UserMapper {
    // 根据ID查询用户
    User selectById(@Param("id") Long id);
    
    // 根据用户名查询用户
    User selectByUsername(@Param("username") String username);

    User selectByCarNumber(@Param("carNumber") String carNumber);
    
    // 根据用户名和密码查询用户
    User selectByUsernameAndPassword(@Param("username") String username, @Param("password") String password);
    
    // 查询所有用户
    List<User> selectAll();
    
    // 插入新用户
    int insert(User user);
    
    // 更新用户信息
    int update(User user);
    
    // 删除用户
    int deleteById(@Param("id") Long id);
} 