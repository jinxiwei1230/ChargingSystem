package com.online.chargingsystem.entity;

import lombok.Data;

@Data
public class User {
    private Long id;
    private String username;
    private String password;
    private String name;
    private String phone;
    private String email;
    private String status; // ACTIVE, INACTIVE
    private Double balance;
    private String role; // USER, VIP
} 