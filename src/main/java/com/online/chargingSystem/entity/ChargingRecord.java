package com.online.chargingSystem.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ChargingRecord {
    private Long id;
    private Long requestId;
    private String pileId;
    private String orderId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal chargeAmount;
    private Double chargingDuration;
    private BigDecimal chargingFee;
    private BigDecimal serviceFee;
    private BigDecimal totalFee;
} 