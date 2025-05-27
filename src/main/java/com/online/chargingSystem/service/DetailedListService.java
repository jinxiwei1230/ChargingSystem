package com.online.chargingSystem.service;

import com.online.chargingSystem.entity.ChargingDetail;
import java.time.LocalDate;
import java.util.List;

public interface DetailedListService {
    // 获取订单详单
    List<ChargingDetail> getOrderDetails(String orderId);
    
    // 获取用户某日详单
    List<ChargingDetail> getUserDailyDetails(Long userId, LocalDate date);
    
    // 获取充电桩详单
    List<ChargingDetail> getPileDetails(String pileId, LocalDate date);
    
    // 生成详单
    List<ChargingDetail> generateDetails(String orderId);
} 