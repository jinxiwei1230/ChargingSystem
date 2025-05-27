package com.online.chargingSystem.controller;

import com.online.chargingSystem.common.Result;
import com.online.chargingSystem.service.QueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 队列控制器
 * 处理充电队列相关的请求，包括查询队列状态、获取排队信息等
 */
@RestController
@RequestMapping("/api/queue")
public class QueueController {

    @Autowired
    private QueueService queueService;

    /**
     * 获取排队信息
     * 根据车辆ID查询当前排队状态
     * @param carId 车辆ID
     * @return 排队信息，包含排队号码、预计等待时间等
     */
    @GetMapping("/info")
    public Result<?> getQueueInfo(@RequestParam String carId) {
        Object result = queueService.getQueueInfo(carId);
        return result != null ? Result.success(result) : Result.error("获取队列信息失败");
    }

    @GetMapping("/state")
    public Object queryQueueState(@RequestParam String queueList) {
        return queueService.queryQueueState(queueList);
    }
} 