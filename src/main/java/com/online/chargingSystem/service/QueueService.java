package com.online.chargingSystem.service;

import com.online.chargingSystem.entity.ChargingQueue;
import java.util.List;

public interface QueueService {
    // 获取车辆排队信息
    ChargingQueue getQueueInfo(String carId);
    
    // 查询队列状态
    List<ChargingQueue> queryQueueState(String pileId);
    
    // 加入队列
    boolean joinQueue(Long requestId, String pileId);
    
    // 离开队列
    boolean leaveQueue(Long requestId);
    
    // 获取队列位置
    int getQueuePosition(Long requestId);
} 