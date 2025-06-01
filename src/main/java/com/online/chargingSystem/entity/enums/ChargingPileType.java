package com.online.chargingSystem.entity.enums;

import lombok.Getter;

@Getter
public enum ChargingPileType {
    FAST("快充"),
    SLOW("慢充");

    private final String description;

    ChargingPileType(String description) {
        this.description = description;
    }

}