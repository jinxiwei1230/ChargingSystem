package com.online.chargingSystem.entity;

import com.online.chargingSystem.entity.enums.ChargingPileStatus;
import com.online.chargingSystem.entity.enums.ChargingPileType;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChargingPile {
    // 充电桩编号(如A/B/C/D/E)
    private String id;
    // 快充/慢充类型
    private ChargingPileType type;
    // 当前状态(AVAILABLE/IN_USE/FAULT/OFFLINE)
    private ChargingPileStatus status;
    // 充电功率(度/小时)
    private Double chargingPower;
    // 累计充电次数
    private Integer chargingTimes;
    // 总充电时长(小时)
    private Double totalChargingDuration;
    // 总充电量(度)
    private Double totalChargingAmount;
    // 创建时间
    private LocalDateTime createdAt;
    // 更新时间
    private LocalDateTime updatedAt;
} 