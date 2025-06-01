package com.online.chargingSystem.entity;

import com.online.chargingSystem.entity.enums.PeriodType;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ChargingDetail {
    // 详单ID
    private Long detailId;
    // 关联订单ID
    private String orderId;
    // 计费时段顺序号(1,2,3...表示跨时段计费)
    private Integer periodSeq;
    // 电价时段类型
    private PeriodType periodType;
    // 时段开始时间
    private LocalDateTime startTime;
    // 时段结束时间
    private LocalDateTime endTime;
    // 充电时长(小时)
    private BigDecimal duration;
    // 充电量(度)
    private BigDecimal kwh;
    // 电费单价(元/度)
    private BigDecimal chargeRate;
    // 服务费单价(元/度)
    private BigDecimal serviceRate;
    // 时段电费
    private BigDecimal chargeFee;
    // 时段服务费
    private BigDecimal serviceFee;
    // 时段合计
    private BigDecimal subTotal;
} 