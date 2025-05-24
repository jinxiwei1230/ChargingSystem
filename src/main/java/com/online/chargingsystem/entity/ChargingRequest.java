package com.online.chargingsystem.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChargingRequest {
    private Long id;
    private String carId;
    private String requestMode; // FAST, TRICKLE
    private Double requestAmount;
    private LocalDateTime requestTime;
    private String status; // PENDING, ACCEPTED, REJECTED, COMPLETED
    private String pileId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double chargingAmount;
    private Double chargingFee;
    private Double serviceFee;
} 