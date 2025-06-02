package com.online.chargingSystem.entity.enums;

public enum PeriodType {
    PEAK("峰时"),
    STANDARD("平时"),
    VALLEY("谷时");

    private final String description;

    PeriodType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
} 