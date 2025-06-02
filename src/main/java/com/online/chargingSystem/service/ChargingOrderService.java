package com.online.chargingSystem.service;

import com.online.chargingSystem.entity.ChargingOrder;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ChargingOrderService {
    /**
     * 根据订单ID获取订单详情
     * @param orderId 订单ID
     * @return 订单详情
     */
    ChargingOrder getOrderById(String orderId);

    /**
     * 获取用户订单列表
     * @param userId 用户ID
     * @param status 订单状态
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param page 页码
     * @param size 每页条数
     * @return 分页订单列表
     */
    Map<String, Object> getUserOrders(Long userId, String status, LocalDate startDate, LocalDate endDate, Integer page, Integer size);

    /**
     * 获取充电桩订单列表
     * @param pileId 充电桩ID
     * @param date 日期
     * @param status 订单状态
     * @return 订单列表
     */
    List<ChargingOrder> getPileOrders(String pileId, LocalDate date, String status);
} 