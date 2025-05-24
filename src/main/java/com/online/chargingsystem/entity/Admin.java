package com.online.chargingsystem.entity;

import lombok.Data;

@Data
public class Admin {
    private Long id;
    private String username;
    private String password;
    private String name;
    private String phone;
    private String email;
    private String status; // ACTIVE, INACTIVE
    private String role; // SUPER_ADMIN, NORMAL_ADMIN
    private String permissions; // 权限列表，JSON格式存储
} 