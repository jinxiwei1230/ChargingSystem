package com.online.chargingsystem.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChargingQueue {
    private Long id;
    private String pileId;
    private String queueType; // FAST, TRICKLE
    private String queueNumber;
    private String carId;
    private String requestMode;
    private Double requestAmount;
    private LocalDateTime queueTime;
    private String status; // WAITING, PROCESSING, COMPLETED
} 