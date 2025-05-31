package com.online.chargingSystem.service.impl;

import com.online.chargingSystem.entity.ChargingRequest;
import com.online.chargingSystem.entity.enums.ChargingPileType;
import com.online.chargingSystem.entity.enums.RequestStatus;
import com.online.chargingSystem.mapper.ChargingRequestMapper;
import com.online.chargingSystem.service.SchedulingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class SchedulingServiceImpl implements SchedulingService {

    @Autowired
    private ChargingRequestMapper chargingRequestMapper;

    // 用于生成快充和慢充的排队序号
    // AtomicInteger是一个提供原子操作的整数类
    private final AtomicInteger fastQueueCounter = new AtomicInteger(0);
    private final AtomicInteger slowQueueCounter = new AtomicInteger(0);

    // 处理充电请求
    @Override
    @Transactional
    public ChargingRequest handleChargingRequest(Long userId, Double requestAmount, ChargingPileType mode) {
        // 创建充电请求
        ChargingRequest request = new ChargingRequest();
        request.setUserId(userId);
        request.setAmount(requestAmount);
        request.setMode(mode);
        request.setStatus(RequestStatus.WAITING_IN_WAITING_AREA);
        request.setRequestTime(LocalDateTime.now());
        request.setQueueJoinTime(LocalDateTime.now());
        
        // 生成排队号码
        String queueNumber = generateQueueNumber(mode);
        request.setQueueNumber(queueNumber);
        
        // 保存充电请求
        chargingRequestMapper.insert(request);
        
        return request;
    }

    /**
     * 生成排队号码
     * @param mode 充电模式（快充/慢充）
     * @return 排队号码（F1/T1格式）
     */
    private String generateQueueNumber(ChargingPileType mode) {
        if (mode == ChargingPileType.FAST) {
            return "F" + fastQueueCounter.incrementAndGet();
        } else {
            return "T" + slowQueueCounter.incrementAndGet();
        }
    }

    @Override
    @Transactional
    public boolean startCharging(Long requestId) {
        // TODO: 实现开始充电逻辑
        return false;
    }

    @Override
    @Transactional
    public boolean endCharging(Long requestId) {
        // TODO: 实现结束充电逻辑
        return false;
    }

    @Override
    public ChargingRequest getChargingStatus(Long requestId) {
        // TODO: 实现获取充电状态逻辑
        return null;
    }

    @Override
    @Transactional
    public boolean interruptCharging(Long requestId) {
        // TODO: 实现中断充电逻辑
        return false;
    }

    @Override
    @Transactional
    public boolean modifyAmount(Long requestId, Double amount) {
        // TODO: 实现修改充电量逻辑
        return false;
    }

    @Override
    @Transactional
    public boolean modifyMode(Long requestId, ChargingPileType mode) {
        // TODO: 实现修改充电模式逻辑
        return false;
    }

    @Override
    public ChargingRequest queryCarState(String userId) {
        // TODO: 实现查询车辆状态逻辑
        return null;
    }

    @Override
    public ChargingRequest queryChargingState(String userId) {
        // TODO: 实现查询充电状态逻辑
        return null;
    }
} 