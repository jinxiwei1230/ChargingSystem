package com.online.chargingSystem.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ChargingReportDTO {
    private String pileId;                    // 充电桩编号
    private LocalDateTime startTime;          // 开始时间
    private LocalDateTime endTime;            // 结束时间
    private Integer totalChargingCount;       // 累计充电次数
    private Double totalChargingDuration;     // 累计充电时长(小时)
    private Double totalChargingAmount;       // 累计充电量(度)
    private BigDecimal totalChargingFee;      // 累计充电费用
    private BigDecimal totalServiceFee;       // 累计服务费用
    private BigDecimal totalFee;              // 累计总费用
} 