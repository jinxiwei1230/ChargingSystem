package com.online.chargingSystem.service;

import com.online.chargingSystem.entity.ChargingRequest;
import com.online.chargingSystem.entity.enums.ChargingPileType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

public interface SchedulingService {
    // 开启叫号
    void startCallNumber();
    // 暂停叫号
    void stopCallNumber();

    // 等候区是否已满，0未满，1满
    boolean isWaitingAreaFull();
    // 请求充电量是否合理，0不合理，1合理
    boolean isRequestAmountValid(Long userId, Double requestAmount);
    // 提交充电请求
    ChargingRequest handleChargingRequest(Long userId, Double requestAmount, ChargingPileType mode);
    // 查看本车排队号码
    String getQueueNumber(Long userId);
    // 获取本充电模式下前车等待数量
    int getAheadNumber(Long userId);
    // 检查车辆是否在等候区
    boolean isInWaitingArea(Long userId);
    // 修改充电模式
    void modifyChargingMode(Long userId, ChargingPileType mode);
    // 修改充电量
    void modifyChargingAmount(Long userId, Double requestAmount);
    // 取消充电并回到等候区重新排队
    void cancelAndRequeue(Long userId);
    // 取消充电并离开
    void cancel(Long userId);

//    boolean canCallNumber();

    ChargingRequest getNextRequest();

    double calculateWaitingTime(String pileId);

    double calculateChargingTime(ChargingRequest request, String pileId);

    String assignOptimalPile(ChargingRequest request);

    void handleCallNumber();

    // 充电完成处理
    void handleChargingComplete(Long requestId);

    boolean canCallNumber();
}