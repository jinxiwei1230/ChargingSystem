package com.online.chargingSystem.controller;

import com.online.chargingSystem.common.Result;
import com.online.chargingSystem.entity.ChargingDetail;
import com.online.chargingSystem.service.DetailedListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

/**
 * 充电详单控制器
 * 处理充电详单相关的请求，包括查询充电详单信息等
 */
@RestController
@RequestMapping("/api/detailed-list")
public class DetailedListController {

    @Autowired
    private DetailedListService detailedListService;

    /**
     * 获取订单详单
     * @param orderId 订单ID
     * @return 订单详单列表
     */
    @GetMapping("/order")
    public Result<?> getOrderDetails(@RequestParam String orderId) {
        List<ChargingDetail> result = detailedListService.getOrderDetails(orderId);
        return result != null ? Result.success(result) : Result.error("获取订单详单失败");
    }

    /**
     * 获取用户充电详单信息
     * @param userId 用户ID
     * @param date 日期（可选）
     * @return 用户充电详单列表
     */
    @GetMapping("/user/{userId}")
    public Result<?> getUserChargingDetails(@PathVariable Long userId,
                                          @RequestParam(required = false) LocalDate date) {
        List<ChargingDetail> result;
        if (date != null) {
            result = detailedListService.getUserDailyDetails(userId, date);
        } else {
            result = detailedListService.getUserChargingDetails(userId);
        }
        return result != null ? Result.success(result) : Result.error("获取用户充电详单失败");
    }
} 