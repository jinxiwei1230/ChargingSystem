package com.online.chargingSystem.entity;

import com.online.chargingSystem.entity.enums.PeriodType;
import lombok.Data;

@Data
public class PricePeriod {
    private int id;
    private PeriodType type;
    private String start;
    private String end;
    private double price;

    public PricePeriod(int id, PeriodType type, String start, String end, double price) {
        this.id = id;
        this.type = type;
        this.start = start;
        this.end = end;
        this.price = price;
    }
} 