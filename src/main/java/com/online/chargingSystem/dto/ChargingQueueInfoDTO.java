package com.online.chargingSystem.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChargingQueueInfoDTO {
    private String pileId;                // 充电桩编号
    private Long userId;                  // 用户ID
    private String carNumber;             // 车牌号
    private Double batteryCapacity;       // 车辆电池总容量
    private Double requestAmount;         // 请求充电量
    private LocalDateTime queueJoinTime;  // 加入队列时间
    private Double waitingTime;           // 预计等待时间（分钟）
    private Integer queuePosition;        // 队列位置
} 