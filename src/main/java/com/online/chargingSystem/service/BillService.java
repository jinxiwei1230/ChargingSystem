package com.online.chargingSystem.service;

import com.online.chargingSystem.entity.ChargingOrder;
import com.online.chargingSystem.entity.ChargingDetail;
import java.time.LocalDate;
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
} 