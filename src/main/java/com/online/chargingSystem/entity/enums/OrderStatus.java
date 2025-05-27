package com.online.chargingSystem.entity.enums;

public enum OrderStatus {
    CREATED("已创建"),
    CHARGING("充电中"),
    COMPLETED("已完成"),
    CANCELLED("已取消"),
    FAULTED("故障终止");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
} 