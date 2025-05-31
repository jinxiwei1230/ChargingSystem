package com.online.chargingSystem.entity.enums;

import lombok.Getter;

//充电请求的状态
@Getter
public enum RequestStatus {
    WAITING_IN_WAITING_AREA("在等待区等待"),
    WAITING_IN_CHARGING_AREA("在充电区等待"),
    CHARGING("充电中"),
    COMPLETED("已完成"),
    CANCELED("已取消");

    private final String description;

    RequestStatus(String description) {
        this.description = description;
    }

}