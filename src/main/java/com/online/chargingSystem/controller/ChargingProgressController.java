package com.online.chargingSystem.controller;

import com.online.chargingSystem.service.ChargingProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/charging-progress")
public class ChargingProgressController {
    
    @Autowired
    private ChargingProgressService chargingProgressService;
    
    /**
     * 获取当前充电进度
     * @param requestId 充电请求ID
     * @return 当前充电量（千瓦时）
     */
    @GetMapping("/{requestId}")
    public double getCurrentProgress(@PathVariable Long requestId) {
        return chargingProgressService.getCurrentProgress(requestId);
    }
    
    /**
     * 检查充电是否完成
     * @param requestId 充电请求ID
     * @return 是否完成
     */
    @GetMapping("/{requestId}/status")
    public boolean isChargingCompleted(@PathVariable Long requestId) {
        return chargingProgressService.isChargingCompleted(requestId);
    }
} 