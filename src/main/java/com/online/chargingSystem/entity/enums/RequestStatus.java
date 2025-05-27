package com.online.chargingSystem.entity.enums;

public enum RequestStatus {
    WAITING("等待中"),
    CHARGING("充电中"),
    COMPLETED("已完成"),
    CANCELED("已取消");

    private final String description;

    RequestStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
} 