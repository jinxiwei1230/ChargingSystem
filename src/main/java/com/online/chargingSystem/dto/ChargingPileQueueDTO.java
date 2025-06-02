package com.online.chargingSystem.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChargingPileQueueDTO {
    // 用户ID
    private Long userId;
    // 车辆电池总容量(度)
    private Double batteryCapacity;
    // 请求充电量(度)
    private Double requestAmount;
    // 排队时长(分钟)
    private Long waitingDuration;
    // 请求ID
    private Long requestId;
    // 排队号
    private String queueNumber;
    // 加入队列时间
    private LocalDateTime queueJoinTime;
    // 充电桩ID
    private String pileId;
} 