package com.online.chargingSystem.service;

import com.online.chargingSystem.entity.ChargingRequest;
import com.online.chargingSystem.entity.enums.ChargingPileType;

public interface ChargingService {
    // 提交充电请求
    ChargingRequest submitChargingRequest(Long userId, String carId, Double requestAmount, ChargingPileType mode);
    
    // 开始充电
    boolean startCharging(Long requestId);
    
    // 结束充电
    boolean endCharging(Long requestId);
    
    // 获取充电状态
    ChargingRequest getChargingStatus(Long requestId);
    
    // 中断充电
    boolean interruptCharging(Long requestId);
    
    // 修改充电量
    boolean modifyAmount(Long requestId, Double amount);
    
    // 修改充电模式
    boolean modifyMode(Long requestId, ChargingPileType mode);
    
    // 查询车辆状态
    ChargingRequest queryCarState(String carId);
    
    // 查询充电状态
    ChargingRequest queryChargingState(String carId);
} 