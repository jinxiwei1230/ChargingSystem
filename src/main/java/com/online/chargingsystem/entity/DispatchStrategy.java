package com.online.chargingsystem.entity;

import lombok.Data;

@Data
public class DispatchStrategy {
    private Long id;
    private String strategyType; // PRIORITY, TIME_ORDER, SINGLE_DISPATCH, BATCH_DISPATCH
    private String description;
    private Boolean isActive;
    private String parameters; // JSON格式存储策略参数
} 