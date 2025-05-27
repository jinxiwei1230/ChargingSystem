package com.online.chargingSystem.entity.enums;

public enum PricePeriodType {
    PEAK("峰时"),
    STANDARD("平时"),
    VALLEY("谷时");

    private final String description;

    PricePeriodType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
} 