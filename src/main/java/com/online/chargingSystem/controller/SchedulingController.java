package com.online.chargingSystem.controller;

import com.online.chargingSystem.common.Result;
import com.online.chargingSystem.entity.ChargingRequest;
import com.online.chargingSystem.entity.enums.ChargingPileType;
import com.online.chargingSystem.service.SchedulingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 充电控制器
 * 处理充电相关的请求，包括开始充电、结束充电、查询充电状态等
 */
@RestController
@RequestMapping("/api/charging")
public class SchedulingController {

    @Autowired
    private SchedulingService schedulingService;

    /**
     * 开始充电
     * 为指定车辆分配充电桩并开始充电
     * @param requestId 充电请求ID
     * @return 充电开始结果
     */
    @PostMapping("/start")
    public Result<?> startCharging(@RequestParam Long requestId) {
        boolean result = schedulingService.startCharging(requestId);
        return result ? Result.success("充电开始成功") : Result.error("充电开始失败");
    }

    /**
     * 结束充电
     * 结束指定车辆的充电并生成账单
     * @param requestId 充电请求ID
     * @return 充电结束结果
     */
    @PostMapping("/end")
    public Result<?> endCharging(@RequestParam Long requestId) {
        boolean result = schedulingService.endCharging(requestId);
        return result ? Result.success("充电结束成功") : Result.error("充电结束失败");
    }

    /**
     * 查询充电状态
     * 获取指定车辆的当前充电状态
     * @param requestId 充电请求ID
     * @return 充电状态信息
     */
    @GetMapping("/status")
    public Result<?> getChargingStatus(@RequestParam Long requestId) {
        ChargingRequest result = schedulingService.getChargingStatus(requestId);
        return result != null ? Result.success(result) : Result.error("获取充电状态失败");
    }

    /**
     * 中断充电
     * 强制中断指定车辆的充电
     * @param requestId 充电请求ID
     * @return 中断结果
     */
    @PostMapping("/interrupt")
    public Result<?> interruptCharging(@RequestParam Long requestId) {
        boolean result = schedulingService.interruptCharging(requestId);
        return result ? Result.success("充电中断成功") : Result.error("充电中断失败");
    }

    @PostMapping("/request")
    public Result<?> submitChargingRequest(@RequestParam Long userId,
                                      @RequestParam String carId,
                                      @RequestParam Double requestAmount,
                                      @RequestParam ChargingPileType mode) {
        ChargingRequest result = schedulingService.handleChargingRequest(userId, requestAmount, mode);
        return result != null ? Result.success("充电申请提交成功", result) : Result.error("充电申请提交失败");
    }

    @PutMapping("/amount")
    public Result<?> modifyAmount(@RequestParam Long requestId,
                             @RequestParam Double amount) {
        boolean result = schedulingService.modifyAmount(requestId, amount);
        return result ? Result.success("充电量修改成功") : Result.error("充电量修改失败");
    }

    @PutMapping("/mode")
    public Result<?> modifyMode(@RequestParam Long requestId,
                           @RequestParam ChargingPileType mode) {
        boolean result = schedulingService.modifyMode(requestId, mode);
        return result ? Result.success("充电模式修改成功") : Result.error("充电模式修改失败");
    }

    @GetMapping("/state")
    public Result<?> queryCarState(@RequestParam String carId) {
        ChargingRequest result = schedulingService.queryCarState(carId);
        return result != null ? Result.success(result) : Result.error("查询车辆状态失败");
    }

    @GetMapping("/charging-state")
    public Result<?> queryChargingState(@RequestParam String carId) {
        ChargingRequest result = schedulingService.queryChargingState(carId);
        return result != null ? Result.success(result) : Result.error("查询充电状态失败");
    }
} 