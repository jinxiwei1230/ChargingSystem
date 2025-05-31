package com.online.chargingSystem.entity;

import lombok.Data;
import java.util.List;

@Data
public class User {
    private Long id;
    private String username;
    private String carNumber;  // 车牌号
    private String password;
}
