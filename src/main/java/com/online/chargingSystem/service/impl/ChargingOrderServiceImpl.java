package com.online.chargingSystem.service.impl;

import com.online.chargingSystem.entity.ChargingOrder;
import com.online.chargingSystem.mapper.ChargingOrderMapper;
import com.online.chargingSystem.service.ChargingOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChargingOrderServiceImpl implements ChargingOrderService {

    @Autowired
    private ChargingOrderMapper chargingOrderMapper;

    @Override
    public ChargingOrder getOrderById(String orderId) {
        return chargingOrderMapper.findById(orderId);
    }

    @Override
    public Map<String, Object> getUserOrders(Long userId, String status, LocalDate startDate, LocalDate endDate, Integer page, Integer size) {
        // 计算偏移量
        int offset = (page - 1) * size;
        
        // 获取总记录数
        int total = chargingOrderMapper.countUserOrders(userId, status, startDate, endDate);
        
        // 获取分页数据
        List<ChargingOrder> orders = chargingOrderMapper.findUserOrders(userId, status, startDate, endDate, offset, size);
        
        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("pages", (total + size - 1) / size);
        result.put("current", page);
        result.put("records", orders);
        
        return result;
    }

    @Override
    public List<ChargingOrder> getPileOrders(String pileId, LocalDate date, String status) {
        return chargingOrderMapper.findPileOrders(pileId, date, status);
    }
} 