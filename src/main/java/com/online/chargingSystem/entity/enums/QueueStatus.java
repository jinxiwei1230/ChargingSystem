package com.online.chargingSystem.entity.enums;

public enum QueueStatus {
    WAITING("等待中"),
    CHARGING("充电中");

    private final String description;

    QueueStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
} 