package com.online.chargingsystem.service;

public interface BillService {
    Object getBillInfo(String billId);
    Object requestBill(String carId, String date);
} 