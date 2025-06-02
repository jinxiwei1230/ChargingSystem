package com.online.chargingSystem.service;

import com.online.chargingSystem.entity.ChargingDetail;
import java.time.LocalDate;
import java.util.List;

public interface DetailedListService {
    /**
     * 获取订单详单
     * @param orderId 订单ID
     * @return 订单详单列表
     */
    List<ChargingDetail> getOrderDetails(String orderId);
    
    /**
     * 获取用户某日详单
     * @param userId 用户ID
     * @param date 日期
     * @return 用户详单列表
     */
    List<ChargingDetail> getUserDailyDetails(Long userId, LocalDate date);
    

    /**
     * 获取用户充电详单信息
     * @param userId 用户ID
     * @return 用户的所有充电详单列表
     */
    List<ChargingDetail> getUserChargingDetails(Long userId);
} 