package com.online.chargingSystem.controller;

import com.online.chargingSystem.common.Result;
import com.online.chargingSystem.entity.ChargingRequest;
import com.online.chargingSystem.service.ChargingRequestService;
import com.online.chargingSystem.service.ChargingProgressService;
import com.online.chargingSystem.entity.ChargingOrder;
import com.online.chargingSystem.mapper.ChargingOrderMapper;
import com.online.chargingSystem.mapper.ChargingRequestMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/charging-requests")
public class ChargingRequestController {

    @Autowired
    private ChargingRequestService chargingRequestService;

    @Autowired
    private ChargingProgressService chargingProgressService;

    @Autowired
    private ChargingOrderMapper chargingOrderMapper;

    @Autowired
    private ChargingRequestMapper chargingRequestMapper;

    @GetMapping("/all")
    public List<ChargingRequest> getAllChargingRequests() {
        return chargingRequestService.findAll();
    }

    @GetMapping("/charging-power/{requestId}")
    public Result<?> getCurrentChargingPower(@PathVariable Long requestId) {
        // 先尝试从监控中获取实时充电量
        double currentPower = chargingProgressService.getCurrentProgress(requestId);
        String status = "CHARGING";  // 默认状态为充电中
        double percentage = 0.0;  // 充电百分比
        
        // 如果不在监控中（返回0），则查询是否有故障订单
        if (currentPower == 0.0) {
            ChargingOrder faultOrder = chargingOrderMapper.findByRequestId(requestId);
            if (faultOrder != null) {
                currentPower = faultOrder.getTotalKwh().doubleValue();
                status = faultOrder.getOrderStatus().name();  // 获取故障订单状态
            } else {
                status = "UNSTART";  // 既不在监控中也没有故障订单
            }
        }

        // 获取目标充电量
        ChargingRequest request = chargingRequestMapper.findById(requestId);
        if (request != null) {
            double targetPower = request.getAmount();
            if (targetPower > 0) {
                percentage = (currentPower / targetPower) * 100;
                // 确保百分比不超过100%
                percentage = Math.min(percentage, 100.0);
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("requestId", requestId);
        // data.put("currentPower", currentPower);
        data.put("percentage", String.format("%.1f", percentage));  // 保留一位小数
        data.put("status", status);
        return Result.success(data);
    }
} 