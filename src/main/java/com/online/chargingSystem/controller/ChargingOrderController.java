package com.online.chargingSystem.controller;

import com.online.chargingSystem.common.Result;
import com.online.chargingSystem.entity.ChargingOrder;
import com.online.chargingSystem.service.ChargingOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

/**
 * 充电订单控制器
 * 处理充电订单相关的请求，包括查询订单信息等
 */
@RestController
@RequestMapping("/api/orders")
public class ChargingOrderController {

    @Autowired
    private ChargingOrderService chargingOrderService;

    /**
     * 获取订单详情
     * @param orderId 订单ID
     * @return 订单详情
     */
    @GetMapping("/{orderId}")
    public Result<?> getOrderDetail(@PathVariable String orderId) {
        ChargingOrder order = chargingOrderService.getOrderById(orderId);
        return order != null ? Result.success(order) : Result.error("订单不存在");
    }

    /**
     * 获取用户订单列表
     * @param userId 用户ID
     * @param status 订单状态（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param page 页码（可选，默认1）
     * @param size 每页条数（可选，默认10）
     * @return 订单列表
     */
    @GetMapping("/user/{userId}")
    public Result<?> getUserOrders(
            @PathVariable Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(chargingOrderService.getUserOrders(userId, status, startDate, endDate, page, size));
    }
} 