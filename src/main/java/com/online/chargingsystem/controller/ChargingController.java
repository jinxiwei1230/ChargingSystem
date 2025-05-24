package com.online.chargingsystem.controller;

import com.online.chargingsystem.common.Result;
import com.online.chargingsystem.service.ChargingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 充电控制器
 * 处理充电相关的请求，包括开始充电、结束充电、查询充电状态等
 */
@RestController
@RequestMapping("/api/charging")
public class ChargingController {

    @Autowired
    private ChargingService chargingService;

    /**
     * 开始充电
     * 为指定车辆分配充电桩并开始充电
     * @param carId 车辆ID
     * @param requestMode 充电模式（快充/慢充）
     * @param requestAmount 请求充电量
     * @return 充电开始结果
     */
    @PostMapping("/start")
    public Result<?> startCharging(@RequestParam String carId,
                              @RequestParam String requestMode,
                              @RequestParam Double requestAmount) {
        Object result = chargingService.startCharging(carId, requestMode, requestAmount);
        return result != null ? Result.success("充电开始成功", result) : Result.error("充电开始失败");
    }

    /**
     * 结束充电
     * 结束指定车辆的充电并生成账单
     * @param carId 车辆ID
     * @return 充电结束结果
     */
    @PostMapping("/end")
    public Result<?> endCharging(@RequestParam String carId) {
        Object result = chargingService.endCharging(carId);
        return result != null ? Result.success("充电结束成功", result) : Result.error("充电结束失败");
    }

    /**
     * 查询充电状态
     * 获取指定车辆的当前充电状态
     * @param carId 车辆ID
     * @return 充电状态信息
     */
    @GetMapping("/status")
    public Result<?> getChargingStatus(@RequestParam String carId) {
        Object result = chargingService.getChargingStatus(carId);
        return result != null ? Result.success(result) : Result.error("获取充电状态失败");
    }

    /**
     * 中断充电
     * 强制中断指定车辆的充电
     * @param carId 车辆ID
     * @return 中断结果
     */
    @PostMapping("/interrupt")
    public Result<?> interruptCharging(@RequestParam String carId) {
        Object result = chargingService.interruptCharging(carId);
        return result != null ? Result.success("充电中断成功", result) : Result.error("充电中断失败");
    }

    @PostMapping("/request")
    public Result<?> submitChargingRequest(@RequestParam String carId,
                                      @RequestParam Double requestAmount,
                                      @RequestParam String requestMode) {
        Object result = chargingService.submitChargingRequest(carId, requestAmount, requestMode);
        return result != null ? Result.success("充电申请提交成功", result) : Result.error("充电申请提交失败");
    }

    @PutMapping("/amount")
    public Result<?> modifyAmount(@RequestParam String carId,
                             @RequestParam Double amount) {
        Object result = chargingService.modifyAmount(carId, amount);
        return result != null ? Result.success("充电量修改成功", result) : Result.error("充电量修改失败");
    }

    @PutMapping("/mode")
    public Result<?> modifyMode(@RequestParam String carId,
                           @RequestParam String mode) {
        Object result = chargingService.modifyMode(carId, mode);
        return result != null ? Result.success("充电模式修改成功", result) : Result.error("充电模式修改失败");
    }

    @GetMapping("/state")
    public Result<?> queryCarState(@RequestParam String carId) {
        Object result = chargingService.queryCarState(carId);
        return result != null ? Result.success(result) : Result.error("查询车辆状态失败");
    }

    @GetMapping("/charging-state")
    public Result<?> queryChargingState(@RequestParam String carId) {
        Object result = chargingService.queryChargingState(carId);
        return result != null ? Result.success(result) : Result.error("查询充电状态失败");
    }
} 