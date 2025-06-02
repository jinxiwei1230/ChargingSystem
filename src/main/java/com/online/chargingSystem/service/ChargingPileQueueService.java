package com.online.chargingSystem.service;

import com.online.chargingSystem.entity.ChargingPileQueue;
import com.online.chargingSystem.entity.enums.ChargingPileStatus;
import com.online.chargingSystem.entity.enums.ChargingPileType;

import java.util.List;
import java.util.Queue;

public interface ChargingPileQueueService {

    // 初始化充电桩队列
    void initPileQueue(String pileId, ChargingPileType type);

    // 获取指定充电桩的队列
    ChargingPileQueue getPileQueue(String pileId);

    // 将请求添加到充电桩队列
    void addToQueue(String pileId, Long requestId);

    // 从充电桩队列中移除请求
    void removeFromQueue(String pileId, Long requestId);

    // 检查充电桩队列是否已满
    boolean isQueueFull(String pileId);

    // 获取充电桩队列大小
    int getQueueSize(String pileId);

    // 获取充电桩队列中的所有请求ID
    Queue<Long> getQueueRequests(String pileId);
    
    // 获取所有故障充电桩的ID列表
    List<String> getFaultPiles();
    
    // 检查充电区是否有空位
    boolean hasPileVacancy();
    
    // 获取指定类型的所有可用充电桩ID列表
    List<String> getAvailableAndInUsePiles(ChargingPileType type);

    /**
     * 打印所有充电桩队列的状态
     */
    void printPileQueues();
}
