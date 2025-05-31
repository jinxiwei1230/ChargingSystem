package com.online.chargingSystem.entity;

import com.online.chargingSystem.entity.enums.ChargingPileType;
import com.online.chargingSystem.entity.enums.RequestStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChargingRequest {
    // 请求唯一ID
    private Long id;
    // 关联用户ID
    private Long userId;
    // 分配的充电桩ID
    private String chargingPileId;
    // 请求状态
    private RequestStatus status;
    // 充电模式(FAST/SLOW)
    private ChargingPileType mode;
    // 请求充电量(度)
    private Double amount;
    // 排队号(F1/T2格式)
    private String queueNumber;
    // 加入队列时间
    private LocalDateTime queueJoinTime;
    // 请求发起时间
    private LocalDateTime requestTime;
}