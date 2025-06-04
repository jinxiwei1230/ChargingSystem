package com.online.chargingSystem.service;

import com.online.chargingSystem.entity.ChargingRequest;
import com.online.chargingSystem.entity.enums.ChargingPileType;

import java.util.Queue;
import java.util.List;
import java.util.Map;

public interface SchedulingService {
    /**
     * 处理充电请求
     * @param userId 用户ID
     * @param requestAmount 请求充电量
     * @param mode 充电模式
     * @return 充电请求
     */
    ChargingRequest handleChargingRequest(Long userId, Double requestAmount, ChargingPileType mode);

    /**
     * 叫号逻辑
     */
    void handleCallNumber();

    /**
     * 判断是否能叫号
     * @return 是否能叫号
     */
    boolean canCallNumber();

    /**
     * 等候区是否已满
     * @return 是否已满
     */
    boolean isWaitingAreaFull();

    /**
     * 请求充电量是否合理
     * @param userId 用户ID
     * @param requestAmount 请求充电量
     * @return 是否合理
     */
    boolean isRequestAmountValid(Long userId, Double requestAmount);

    /**
     * 获取排队号码
     * @param userId 用户ID
     * @return 排队号码
     */
    String getQueueNumber(Long userId);

    /**
     * 获取本充电模式下前车等待数量
     * @param userId 用户ID
     * @return 前车等待数量
     */
    int getAheadNumber(Long userId);

    /**
     * 修改充电模式
     * @param userId 用户ID
     * @param mode 充电模式
     */
    void modifyChargingMode(Long userId, ChargingPileType mode);

    /**
     * 修改充电量
     * @param userId 用户ID
     * @param requestAmount 请求充电量
     */
    void modifyChargingAmount(Long userId, Double requestAmount);

    /**
     * 取消充电并回到等候区重新排队
     * @param userId 用户ID
     */
    void cancelAndRequeue(Long userId);

    /**
     * 取消充电并离开
     * @param userId 用户ID
     */
    void cancel(Long userId);

    /**
     * 充电完成处理
     * @param userId 用户ID
     * @return 是否处理成功
     */
    boolean handleChargingComplete(Long userId);

    /**
     * 计算等候时间
     * @param pileId 充电桩ID
     * @return 等候时间
     */
    double calculateWaitingTime(String pileId);

    /**
     * 计算充电时间
     * @param request 充电请求
     * @param pileId 充电桩ID
     * @return 充电时间
     */
    double calculateChargingTime(ChargingRequest request, String pileId);

    /**
     * 选择最优充电桩
     * @param request 充电请求
     * @return 最优充电桩ID
     */
    String assignOptimalPile(ChargingRequest request);

    /**
     * 判断是否在等候区
     * @param userId 用户ID
     * @return 是否在等候区
     */
    boolean isInWaitingArea(Long userId);

    /**
     * 判断是否在充电等候区
     * @param userId 用户ID
     * @return 是否在充电等候区
     */
    boolean isInChargingArea(Long userId);

    // 判断是否在充电
    boolean isCharging(Long userId);

    /**
     * 开启叫号
     */
    void startCallNumber();

    /**
     * 停止叫号
     */
    void stopCallNumber();

    boolean processQueue(Queue<Long> faultQueue, ChargingPileType pileType);

    /**
     * 获取等候区队列
     * @return 按请求ID排序的等候区请求列表
     */
    List<ChargingRequest> getWaitingAreaQueue();

    /**
     * 获取所有充电桩队列
     * @return 充电桩ID和对应队列的映射
     */
    Map<String, List<ChargingRequest>> getAllPileQueues();

}