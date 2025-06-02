package com.online.chargingSystem.entity;

import com.online.chargingSystem.entity.enums.OrderStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ChargingOrder {
    // 订单号(例：ORD20231125F1001)
    private String orderId;
    // 关联用户ID
    private Long userId;
    // 车辆ID
    private String carId;
    // 关联充电请求
    private Long requestId;
    // 充电桩编号
    private String pileId;
    // 订单日期(用于按日查询)
    private LocalDate orderDate;
    // 订单状态(CREATED/CHARGING/COMPLETED/CANCELLED/FAULTED)
    private OrderStatus orderStatus;
    // 总充电量(度)
    private BigDecimal totalKwh;
    // 总充电时长(小时)
    private BigDecimal totalDuration;
    // 累计电费
    private BigDecimal totalChargeFee;
    // 累计服务费
    private BigDecimal totalServiceFee;
    // 订单总额(电费+服务费)
    private BigDecimal totalFee;
    // 充电功率(度/小时)
    private BigDecimal chargingPower;
    // 充电开始时间
    private LocalDateTime startTime;
    // 充电结束时间
    private LocalDateTime endTime;
    // 订单创建时间
    private LocalDateTime createTime;
} 