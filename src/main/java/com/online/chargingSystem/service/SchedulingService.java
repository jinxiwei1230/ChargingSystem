package com.online.chargingSystem.service;

import com.online.chargingSystem.entity.ChargingRequest;
import com.online.chargingSystem.entity.enums.ChargingPileType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

public interface SchedulingService {

    // 叫号逻辑
    @Transactional
    void handleCallNumber();

    // 判断是否能叫号
    boolean canCallNumber();

    // 处理充电请求
    @Transactional
    ChargingRequest handleChargingRequest(Long userId, Double requestAmount, ChargingPileType mode);

    // 等候区是否已满，0未满，1满
    boolean isWaitingAreaFull();

    // 请求充电量是否合理，0不合理，1合理
    boolean isRequestAmountValid(Long userId, Double requestAmount);

    // 获取排队号码
    String getQueueNumber(Long userId);

    // 获取本充电模式下前车等待数量
    int getAheadNumber(Long userId);

    // 修改充电模式
    @Transactional
    void modifyChargingMode(Long userId, ChargingPileType mode);

    // 修改充电量
    @Transactional
    void modifyChargingAmount(Long userId, Double requestAmount);

    // 取消充电并回到等候区重新排队
    @Transactional
    void cancelAndRequeue(Long userId);

    // 取消充电并离开
    @Transactional
    void cancel(Long userId);

    // 充电完成处理
    @Transactional
    boolean handleChargingComplete(Long userId);

    // 计算等候时间
    double calculateWaitingTime(String pileId);

    // 计算充电时间
    double calculateChargingTime(ChargingRequest request, String pileId);

    // 选择最优充电桩
    String assignOptimalPile(ChargingRequest request);

    // 判断是否在等候区
    boolean isInWaitingArea(Long userId);

    // 判断是否在充电等候区
    boolean isInChargingArea(Long userId);

    // 开启叫号
    void startCallNumber();

    // 停止叫号
    void stopCallNumber();
}