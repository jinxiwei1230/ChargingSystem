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
     * 获取用户某日详单
     * @param userId 用户ID
     * @param date 日期
     * @return 用户详单列表
     */
    @GetMapping("/user")
    public Result<?> getUserDailyDetails(@RequestParam Long userId,
                                    @RequestParam LocalDate date) {
        List<ChargingDetail> result = detailedListService.getUserDailyDetails(userId, date);
        return result != null ? Result.success(result) : Result.error("获取用户详单失败");
    }

    /**
     * 获取充电桩详单
     * @param pileId 充电桩ID
     * @param date 日期
     * @return 充电桩详单列表
     */
    @GetMapping("/pile")
    public Result<?> getPileDetails(@RequestParam String pileId,
                               @RequestParam LocalDate date) {
        List<ChargingDetail> result = detailedListService.getPileDetails(pileId, date);
        return result != null ? Result.success(result) : Result.error("获取充电桩详单失败");
    }

    /**
     * 生成详单
     * @param orderId 订单ID
     * @return 生成的详单列表
     */
    @PostMapping("/generate")
    public Result<?> generateDetails(@RequestParam String orderId) {
        List<ChargingDetail> result = detailedListService.generateDetails(orderId);
        return result != null ? Result.success("详单生成成功", result) : Result.error("详单生成失败");
    }
} 