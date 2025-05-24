package com.online.chargingsystem.service.impl;

import com.online.chargingsystem.entity.ChargingQueue;
import com.online.chargingsystem.mapper.ChargingQueueMapper;
import com.online.chargingsystem.service.QueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class QueueServiceImpl implements QueueService {

    @Autowired
    private ChargingQueueMapper chargingQueueMapper;

    @Override
    public Object getQueueInfo(String carId) {
        // 1. 查询车辆所在的队列
        List<ChargingQueue> queues = chargingQueueMapper.findByCarId(carId);
        if (queues.isEmpty()) {
            return null;
        }

        ChargingQueue queue = queues.get(0);
        Map<String, Object> result = new HashMap<>();
        
        // 2. 计算排队信息
        result.put("queueNumber", queue.getQueueNumber());
        result.put("queueType", queue.getQueueType());
        result.put("requestMode", queue.getRequestMode());
        result.put("requestAmount", queue.getRequestAmount());
        result.put("queueTime", queue.getQueueTime());
        result.put("status", queue.getStatus());

        // 3. 计算预计等待时间
        if ("WAITING".equals(queue.getStatus())) {
            List<ChargingQueue> waitingQueues = chargingQueueMapper.findByQueueTypeAndStatus(
                queue.getQueueType(), "WAITING");
            int position = waitingQueues.indexOf(queue) + 1;
            result.put("position", position);
            result.put("estimatedWaitTime", position * 30); // 假设每辆车平均充电30分钟
        }

        return result;
    }

    @Override
    public Object queryQueueState(String queueList) {
        // 1. 解析队列列表
        String[] queueIds = queueList.split(",");
        Map<String, Object> result = new HashMap<>();
        
        // 2. 查询每个队列的状态
        for (String queueId : queueIds) {
            ChargingQueue queue = chargingQueueMapper.findById(Long.parseLong(queueId));
            if (queue != null) {
                Map<String, Object> queueInfo = new HashMap<>();
                queueInfo.put("queueNumber", queue.getQueueNumber());
                queueInfo.put("carId", queue.getCarId());
                queueInfo.put("status", queue.getStatus());
                queueInfo.put("queueTime", queue.getQueueTime());
                
                // 计算等待时间
                if ("WAITING".equals(queue.getStatus())) {
                    Duration waitTime = Duration.between(queue.getQueueTime(), LocalDateTime.now());
                    queueInfo.put("waitTime", waitTime.toMinutes());
                }
                
                result.put(queueId, queueInfo);
            }
        }
        
        return result;
    }
} 