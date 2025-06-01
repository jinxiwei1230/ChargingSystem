package com.online.chargingSystem.entity;

import lombok.Data;
import java.util.List;

@Data
public class User {
    private Long id;
    private String username;
    // 车牌号
    private String carNumber;
    // 车辆电池总容量(度)
    private Double batteryCapacity;
    private String password;
}
