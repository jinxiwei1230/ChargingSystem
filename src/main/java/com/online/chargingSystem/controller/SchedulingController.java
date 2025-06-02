package com.online.chargingSystem.controller;

import com.online.chargingSystem.common.Result;
import com.online.chargingSystem.entity.ChargingRequest;
import com.online.chargingSystem.entity.WaitingQueue;
import com.online.chargingSystem.entity.enums.ChargingPileType;
import com.online.chargingSystem.service.SchedulingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 调度控制器
 * 处理充电请求
 */
@RestController
@RequestMapping("/request")
public class SchedulingController {

    @Autowired
    private SchedulingService schedulingService;


    // 提交充电请求
    @RequestMapping("/submit")
    public Result<?> submitChargingRequest(@RequestParam Long userId,
                                           @RequestParam Double requestAmount,
                                           @RequestParam ChargingPileType mode) {
        if (schedulingService.isWaitingAreaFull()) {
            return Result.error("等候区已满");
        }
        if (!schedulingService.isRequestAmountValid(userId, requestAmount)) {
            return Result.error("申请充电量超出范围");
        }
        ChargingRequest result = schedulingService.handleChargingRequest(userId, requestAmount, mode);
        return result != null ? Result.success("充电申请提交成功", result) : Result.error("充电申请提交失败");
    }

    // 查看本车排队号码（返回null表示还没有分配排队号码）
    @RequestMapping("/getQueueNumber")
    public Result<?> getQueueNumber(@RequestParam Long userId) {
        String result = schedulingService.getQueueNumber(userId);
        return Result.success("获取成功", result);
    }

    // 查看本充电模式下前车等待数量
    @RequestMapping("/getAheadNumber")
    public Result<?> getAheadNumber(@RequestParam Long userId) {
        int result = schedulingService.getAheadNumber(userId);
        return Result.success("获取成功", result);
    }

    // 修改充电模式（如果请求的模式和原来的相同，就不要调用该接口）
    @RequestMapping("/modifyMode")
    public Result<?> modifyChargingMode(@RequestParam Long userId,
                                        @RequestParam ChargingPileType mode) {
        if (!schedulingService.isInWaitingArea(userId)) {
            return Result.error("车辆不在等候区，无法修改充电模式");
        }
        schedulingService.modifyChargingMode(userId, mode);
        return Result.success("充电模式修改成功");
    }

    // 修改充电量
    @RequestMapping("/modifyAmount")
    public Result<?> modifyChargingMode(@RequestParam Long userId,
                                        @RequestParam Double requestAmount) {
        if (!schedulingService.isInWaitingArea(userId)) {
            return Result.error("车辆不在等候区，无法修改充电量");
        }
        if (!schedulingService.isRequestAmountValid(userId, requestAmount)) {
            return Result.error("申请充电量超出范围");
        }
        schedulingService.modifyChargingAmount(userId, requestAmount);
        return Result.success("充电量修改成功");
    }

    // 取消充电并回到等候区重新排队
    @RequestMapping("/cancelAndRequeue")
    public Result<?> cancelAndRequeue(@RequestParam Long userId) {
        schedulingService.cancelAndRequeue(userId);
        return Result.success("取消充电并重新排队成功");
    }

    // 取消充电并离开
    @RequestMapping("/cancelAndLeave")
    public Result<?> cancelAndLeave(@RequestParam Long userId) {
        schedulingService.cancel(userId);
        return Result.success("取消充电并离开成功");
    }



}