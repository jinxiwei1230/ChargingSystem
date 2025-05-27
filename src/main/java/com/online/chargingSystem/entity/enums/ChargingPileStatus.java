package com.online.chargingSystem.entity.enums;

public enum ChargingPileStatus {
    AVAILABLE("可用"),
    IN_USE("使用中"),
    FAULT("故障"),
    OFFLINE("离线");

    private final String description;

    ChargingPileStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
} 