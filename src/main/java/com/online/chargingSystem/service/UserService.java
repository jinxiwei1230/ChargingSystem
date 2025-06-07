package com.online.chargingSystem.service;

import com.online.chargingSystem.entity.User;
import com.online.chargingSystem.entity.ChargingRequest;
import java.util.List;

public interface UserService {

    /**
     * 登录业务逻辑
     * @param uname 账户名
     * @param password 密码
     * @return
     */
    User loginService(String uname, String password);

    /**
     * 注册业务逻辑
     * @param user 要注册的User对象，属性中主键uid要为空，若uid不为空可能会覆盖已存在的user
     * @return
     */
    User registService(User user);

    /**
     * 获取用户信息
     * @param userId 用户ID
     * @return 用户信息
     */
    User getUserInfo(Long userId);

    /**
     * 获取用户充电请求列表
     * @param userId 用户ID
     * @return 充电请求列表
     */
    List<ChargingRequest> getUserChargingRequests(Long userId);
}