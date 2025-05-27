package com.online.chargingSystem.entity;

import com.online.chargingSystem.entity.enums.ChargingPileType;
import com.online.chargingSystem.entity.enums.QueueStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChargingQueue {
    // 队列条目ID
    private Long id;
    // 充电桩ID
    private String pileId;
    // 充电请求ID
    private Long requestId;
    // 车辆ID
    private String carId;
    // 队列类型(FAST/SLOW)
    private ChargingPileType queueType;
    // 队列状态(WAITING/CHARGING)
    private QueueStatus queueStatus;
    // 队列位置(1-2)
    private Byte position;
    // 加入队列时间
    private LocalDateTime createdAt;
    // 最后更新时间
    private LocalDateTime updatedAt;
} 