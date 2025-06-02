package com.online.chargingSystem.service;

import com.online.chargingSystem.entity.ChargingOrder;
import com.online.chargingSystem.entity.ChargingDetail;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

public interface BillService {
    // 获取账单信息
    ChargingOrder getBillInfo(String billId);
    
    // 请求账单
    ChargingOrder requestBill(String carId, String requestMode);
    
    // 获取订单信息
    ChargingOrder getOrderInfo(String orderId);
    
    // 获取订单详单
    List<ChargingDetail> getOrderDetails(String orderId);
    
    // 获取用户订单
    List<ChargingOrder> getUserOrders(Long userId, LocalDate date);
    
    // 生成订单
    ChargingOrder generateOrder(Long requestId);
    
    // 计算费用
    void calculateFees(String orderId);
    
    /**
     * 计算充电详单
     * @param orderId 订单ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param totalKwh 总充电量（度）
     * @param chargingPower 充电功率（度/小时）
     * @return 充电详单列表
     */
    List<ChargingDetail> calculateChargingDetails(
            String orderId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            BigDecimal totalKwh,
            BigDecimal chargingPower);
} 