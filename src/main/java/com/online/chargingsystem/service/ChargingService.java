package com.online.chargingsystem.service;

public interface ChargingService {
    Object startCharging(String carId, String requestMode, Double requestAmount);
    Object endCharging(String carId);
    Object getChargingStatus(String carId);
    Object interruptCharging(String carId);
    Object submitChargingRequest(String carId, Double requestAmount, String requestMode);
    Object modifyAmount(String carId, Double amount);
    Object modifyMode(String carId, String mode);
    Object queryCarState(String carId);
    Object queryChargingState(String carId);
} 