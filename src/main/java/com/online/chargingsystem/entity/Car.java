package com.online.chargingsystem.entity;

import lombok.Data;

@Data
public class Car {
    private Long id;
    private String carId;
    private String userId;
    private String brand;
    private String model;
    private String batteryCapacity;
    private String chargingType; // FAST, TRICKLE
    private String status; // IDLE, CHARGING, QUEUING
} 