package com.online.chargingSystem.controller;

import com.online.chargingSystem.common.Result;
import com.online.chargingSystem.entity.ChargingRequest;
import com.online.chargingSystem.entity.WaitingQueue;
import com.online.chargingSystem.entity.enums.ChargingPileType;
import com.online.chargingSystem.service.FaultService;
import com.online.chargingSystem.service.SchedulingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 调度控制器
 * 处理充电请求
 */
@RestController
@RequestMapping("/request")
public class SchedulingController {

    @Autowired
    private SchedulingService schedulingService;

    @Autowired
    private FaultService faultService;

    /**
     * 获取等候区队列
     * @return 按请求ID排序的等候区请求列表
     */
    @GetMapping("/waiting-queue")
    public Result<?> getWaitingAreaQueue() {
        List<ChargingRequest> queue = schedulingService.getWaitingAreaQueue();
        return Result.success("获取等候区队列成功", queue);
    }

    /**
     * 获取所有充电桩队列
     * @return 充电桩ID和对应队列的映射
     */
    @GetMapping("/pile-queues")
    public Result<?> getAllPileQueues() {
        Map<String, List<ChargingRequest>> queues = schedulingService.getAllPileQueues();
        return Result.success("获取充电桩队列成功", queues);
    }

    /**
     * 提交充电请求
     * @param userId 用户ID
     * @param requestAmount 请求充电量
     * @param mode 充电模式
     * @return 处理结果
     */
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

    /**
     * 查看本车排队号码
     * @param userId 用户ID
     * @return 排队号码（返回null表示还没有分配排队号码）
     */
    @RequestMapping("/getQueueNumber")
    public Result<?> getQueueNumber(@RequestParam Long userId) {
        String result = schedulingService.getQueueNumber(userId);
        return Result.success("获取成功", result);
    }

    /**
     * 查看本充电模式下前车等待数量
     * @param userId 用户ID
     * @return 前车等待数量
     */
    @RequestMapping("/getAheadNumber")
    public Result<?> getAheadNumber(@RequestParam Long userId) {
        int result = schedulingService.getAheadNumber(userId);
        return Result.success("获取成功", result);
    }

    /**
     * 修改充电模式
     * @param userId 用户ID
     * @param mode 充电模式
     * @return 处理结果
     */
    @RequestMapping("/modifyMode")
    public Result<?> modifyChargingMode(@RequestParam Long userId,
                                        @RequestParam ChargingPileType mode) {
        if (!schedulingService.isInWaitingArea(userId)) {
            return Result.error("车辆不在等候区，无法修改充电模式");
        }
        schedulingService.modifyChargingMode(userId, mode);
        return Result.success("充电模式修改成功");
    }

    /**
     * 修改充电量
     * @param userId 用户ID
     * @param requestAmount 请求充电量
     * @return 处理结果
     */
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

    /**
     * 取消充电并回到等候区重新排队
     * @param userId 用户ID
     * @return 处理结果
     */
    @RequestMapping("/cancelAndRequeue")
    public Result<?> cancelAndRequeue(@RequestParam Long userId) {
        if (!(schedulingService.isInWaitingArea(userId) || schedulingService.isInChargingArea(userId))) {
            return Result.error("车辆不在等候状态，无法取消充电");
        }
        schedulingService.cancelAndRequeue(userId);
        return Result.success("取消充电并重新排队成功");
    }

    /**
     * 取消充电并离开
     * @param userId 用户ID
     * @return 处理结果
     */
    @RequestMapping("/cancelAndLeave")
    public Result<?> cancelAndLeave(@RequestParam Long userId) {
        if (!(schedulingService.isInWaitingArea(userId) || schedulingService.isInChargingArea(userId))) {
            return Result.error("车辆不在等候状态，无法取消充电");
        }
        schedulingService.cancel(userId);
        return Result.success("取消充电并离开成功");
    }

    /**
     * 结束充电
     * @param userId 用户ID
     * @return 处理结果
     */
    @RequestMapping("/finish")
    public Result<?> finish(@RequestParam Long userId) {
        if (!schedulingService.isCharging(userId)) {
            return Result.error("车辆不在充电状态，无法结束充电");
        }
        boolean result = schedulingService.handleChargingComplete(userId);
        return result ? Result.success("结束充电成功") : Result.error("结束充电失败");
    }

    /**
     * 充电桩故障发生
     * @param pileId 故障充电桩ID
     * @param strategy 调度策略（PRIORITY-优先级调度，TIME_ORDER-时间顺序调度）
     * @return 处理结果
     */
    @PostMapping("/fault/handle")
    public Result<?> handlePileFault(@RequestParam String pileId,
                                   @RequestParam String strategy) {
        try {
            faultService.handlePileFault(pileId, strategy);
            return Result.success("故障处理成功");
        } catch (Exception e) {
            return Result.error("故障处理失败：" + e.getMessage());
        }
    }

    /**
     * 处理充电桩故障恢复
     * @param pileId 恢复的充电桩ID
     * @return 处理结果
     */
    @PostMapping("/fault/recovery")
    public Result<?> handlePileRecovery(@RequestParam String pileId) {
        try {
            faultService.handlePileRecovery(pileId);
            return Result.success("故障恢复处理成功");
        } catch (Exception e) {
            return Result.error("故障恢复处理失败：" + e.getMessage());
        }
    }

}