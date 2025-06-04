package com.online.chargingSystem.service;

import com.online.chargingSystem.entity.enums.ChargingPileType;

public interface FaultService {
    /**
     * 处理充电桩故障
     * @param pileId 故障充电桩ID
     * @param strategy 调度策略（PRIORITY-优先级调度，TIME_ORDER-时间顺序调度）
     */
    void handlePileFault(String pileId, String strategy);

    /**
     * 定时处理故障队列
     */
    void scheduledHandleFaultQueue();

    /**
     * 处理优先级调度策略
     * @return 是否所有请求都已处理完毕
     */
    boolean handlePriorityStrategy(java.util.Queue<Long> faultQueue, java.util.List<String> availablePiles, ChargingPileType pileType);

    /**
     * 处理时间顺序调度策略
     * @return 是否所有请求都已处理完毕
     */
    boolean handleTimeOrderStrategy(java.util.Queue<Long> faultQueue, java.util.List<String> availablePiles, ChargingPileType pileType);

    /**
     * 处理充电桩故障恢复
     */
    void handlePileRecovery(String pileId);

    /**
     * 定时处理故障恢复队列
     */
    void scheduledHandleFaultRecoveryQueue();
} 