package com.online.chargingSystem.controller;

import com.online.chargingSystem.common.Result;
import com.online.chargingSystem.entity.ChargingRequest;
import com.online.chargingSystem.service.DispatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

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
     * @param requestId 请求ID
     * @return 调度结果
     */
    @PostMapping("/priority")
    public Result<?> priorityDispatch(@RequestParam Long requestId) {
        boolean result = dispatchService.priorityDispatch(requestId);
        return result ? Result.success("优先级调度成功") : Result.error("优先级调度失败");
    }

    /**
     * 时间顺序调度
     * @param requestId 请求ID
     * @return 调度结果
     */
    @PostMapping("/time-order")
    public Result<?> timeOrderDispatch(@RequestParam Long requestId) {
        boolean result = dispatchService.timeOrderDispatch(requestId);
        return result ? Result.success("时间顺序调度成功") : Result.error("时间顺序调度失败");
    }

    /**
     * 故障恢复调度
     * @param pileId 充电桩ID
     * @return 调度结果
     */
    @PostMapping("/fault-recover")
    public Result<?> faultRecover(@RequestParam String pileId) {
        boolean result = dispatchService.faultRecover(pileId);
        return result ? Result.success("故障恢复调度成功") : Result.error("故障恢复调度失败");
    }

    /**
     * 单次调度
     * @param requestIds 请求ID列表
     * @return 调度结果
     */
    @PostMapping("/single")
    public Result<?> singleDispatch(@RequestBody List<Long> requestIds) {
        List<ChargingRequest> result = dispatchService.singleDispatch(requestIds);
        return result != null ? Result.success("单次调度成功", result) : Result.error("单次调度失败");
    }

    /**
     * 批量调度
     * @param requestIds 请求ID列表
     * @return 调度结果
     */
    @PostMapping("/batch")
    public Result<?> batchDispatch(@RequestBody List<Long> requestIds) {
        List<ChargingRequest> result = dispatchService.batchDispatch(requestIds);
        return result != null ? Result.success("批量调度成功", result) : Result.error("批量调度失败");
    }

    /**
     * 获取最优调度方案
     * @param requestId 请求ID
     * @return 最优充电桩ID
     */
    @GetMapping("/optimal")
    public Result<?> getOptimalPile(@RequestParam Long requestId) {
        String result = dispatchService.getOptimalPile(requestId);
        return result != null ? Result.success(result) : Result.error("获取最优调度方案失败");
    }
} 