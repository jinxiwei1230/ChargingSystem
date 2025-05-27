package com.online.chargingSystem.entity.enums;

public enum ChargingPileType {
    FAST("快充"),
    SLOW("慢充");

    private final String description;

    ChargingPileType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
} 