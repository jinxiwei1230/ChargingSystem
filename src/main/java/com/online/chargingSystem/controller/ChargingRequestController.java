package com.online.chargingSystem.controller;

import com.online.chargingSystem.common.Result;
import com.online.chargingSystem.entity.ChargingRequest;
import com.online.chargingSystem.service.ChargingRequestService;
import com.online.chargingSystem.service.ChargingProgressService;
import com.online.chargingSystem.entity.ChargingOrder;
import com.online.chargingSystem.mapper.ChargingOrderMapper;
import com.online.chargingSystem.mapper.ChargingRequestMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/charging-requests")
public class ChargingRequestController {
    private static final Logger logger = LoggerFactory.getLogger(ChargingRequestController.class);

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
        
        // 如果不在监控中（返回0），则查询是否有故障订单或已完成订单
        if (currentPower == 0.0) {
            ChargingOrder faultOrder = chargingOrderMapper.findByRequestId(requestId);
            if (faultOrder != null) {
                currentPower = faultOrder.getTotalKwh().doubleValue();
                status = faultOrder.getOrderStatus().name();  // 获取故障订单或已完成订单状态
            } else {
                status = "UNSTART";  // 既不在监控中也没有故障订单设置为未开始
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

    @GetMapping("/current-charging")
    public Result<?> getCurrentChargingRequests() {
        logger.info("开始获取所有正在充电的请求信息");
        
        // 获取所有充电请求
        List<ChargingRequest> allRequests = chargingRequestService.findAll();
        List<Map<String, Object>> chargingInfoList = new ArrayList<>();
        
        for (ChargingRequest request : allRequests) {
            if (request.getStatus().name().equals("CHARGING")) {
                double currentPower = chargingProgressService.getCurrentProgress(request.getId());
                double targetPower = request.getAmount();
                double percentage = 0.0;
                
                if (targetPower > 0) {
                    percentage = (currentPower / targetPower) * 100;
                    percentage = Math.min(percentage, 100.0);
                }
                
                Map<String, Object> chargingInfo = new HashMap<>();
                chargingInfo.put("requestId", request.getId());
                chargingInfo.put("userId", request.getUserId());
                chargingInfo.put("currentPower", String.format("%.2f", currentPower));
                chargingInfo.put("targetPower", String.format("%.2f", targetPower));
                chargingInfo.put("percentage", String.format("%.1f", percentage));
                
                chargingInfoList.add(chargingInfo);
                
                logger.info("充电请求ID: {}, 用户ID: {}, 当前充电量: {}kWh, 目标充电量: {}kWh, 完成度: {}%", 
                    request.getId(), 
                    request.getUserId(), 
                    String.format("%.2f", currentPower),
                    String.format("%.2f", targetPower),
                    String.format("%.1f", percentage));
            }
        }
        
        logger.info("共找到 {} 个正在充电的请求", chargingInfoList.size());
        return Result.success(chargingInfoList);
    }
} 