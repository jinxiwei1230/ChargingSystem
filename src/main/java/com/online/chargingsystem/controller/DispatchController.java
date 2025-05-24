package com.online.chargingsystem.controller;

import com.online.chargingsystem.common.Result;
import com.online.chargingsystem.service.DispatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 调度控制器
 * 处理充电桩调度相关的请求，包括优先级调度、时间顺序调度等
 */
@RestController
@RequestMapping("/api/dispatch")
public class DispatchController {

    @Autowired
    private DispatchService dispatchService;

    /**
     * 优先级调度
     * 根据优先级策略分配充电桩
     * @param carId 车辆ID
     * @return 调度结果
     */
    @PostMapping("/priority")
    public Result<?> priorityDispatch(@RequestParam String carId) {
        Object result = dispatchService.priorityDispatch(carId);
        return result != null ? Result.success("优先级调度成功", result) : Result.error("优先级调度失败");
    }

    /**
     * 时间顺序调度
     * 根据排队时间顺序分配充电桩
     * @param carId 车辆ID
     * @return 调度结果
     */
    @PostMapping("/time-order")
    public Result<?> timeOrderDispatch(@RequestParam String carId) {
        Object result = dispatchService.timeOrderDispatch(carId);
        return result != null ? Result.success("时间顺序调度成功", result) : Result.error("时间顺序调度失败");
    }

    /**
     * 故障恢复调度
     * 处理故障充电桩恢复后的重新调度
     * @param pileId 充电桩ID
     * @return 调度结果
     */
    @PostMapping("/fault-recover")
    public Result<?> faultRecover(@RequestParam String pileId) {
        Object result = dispatchService.faultRecover(pileId);
        return result != null ? Result.success("故障恢复调度成功", result) : Result.error("故障恢复调度失败");
    }

    /**
     * 单个车辆调度
     * 为单个车辆分配充电桩
     * @param carId 车辆ID
     * @return 调度结果
     */
    @PostMapping("/single")
    public Result<?> singleDispatch(@RequestParam String carId) {
        Object result = dispatchService.singleDispatch(carId);
        return result != null ? Result.success("单个车辆调度成功", result) : Result.error("单个车辆调度失败");
    }

    /**
     * 批量调度
     * 为多个车辆分配充电桩
     * @param carIds 车辆ID列表
     * @return 调度结果
     */
    @PostMapping("/batch")
    public Result<?> batchDispatch(@RequestBody String[] carIds) {
        Object result = dispatchService.batchDispatch(carIds);
        return result != null ? Result.success("批量调度成功", result) : Result.error("批量调度失败");
    }
} 