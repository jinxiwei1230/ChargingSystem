package com.online.chargingsystem.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChargingPileFault {
    private Long id;
    private String pileId;
    private String faultType; // HARDWARE, SOFTWARE, POWER
    private String status; // ACTIVE, RESOLVED
    private LocalDateTime faultTime;
    private LocalDateTime resolveTime;
    private String description;
    private String resolution;
} 