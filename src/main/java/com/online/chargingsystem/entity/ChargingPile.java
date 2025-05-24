package com.online.chargingsystem.entity;

import lombok.Data;

@Data
public class ChargingPile {
    private Long id;
    private String pileId;
    private String type; // FAST, TRICKLE
    private String status; // IDLE, CHARGING, FAULT
    private Double power; // 充电功率
    private String currentChargingCar;
    private Double totalChargingTime;
    private Double totalChargingAmount;
    private Double totalChargingFee;
    private Double totalServiceFee;
} 