package com.online.chargingsystem.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChargingRecord {
    private Long id;
    private String carId;
    private String pileId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double chargingAmount;
    private Double chargingFee;
    private Double serviceFee;
    private String status; // CHARGING, COMPLETED, INTERRUPTED
} 