package com.online.chargingsystem.service.impl;

import com.online.chargingsystem.entity.ChargingRecord;
import com.online.chargingsystem.mapper.ChargingRecordMapper;
import com.online.chargingsystem.service.DetailedListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
public class DetailedListServiceImpl implements DetailedListService {

    @Autowired
    private ChargingRecordMapper chargingRecordMapper;

    @Override
    public Object requestDetailedList(String billId) {
        ChargingRecord chargingRecord = chargingRecordMapper.findById(Long.parseLong(billId));
        
        if (chargingRecord != null) {
            Map<String, Object> result = new HashMap<>();
            
            result.put("carId", chargingRecord.getCarId());
            result.put("date", chargingRecord.getStartTime().toLocalDate());
            result.put("billId", chargingRecord.getId());
            result.put("chargePileNum", chargingRecord.getPileId());
            result.put("chargeAmount", chargingRecord.getChargingAmount());
            result.put("chargeDuration", Duration.between(chargingRecord.getStartTime(), chargingRecord.getEndTime()).toHours());
            result.put("startTime", chargingRecord.getStartTime());
            result.put("endTime", chargingRecord.getEndTime());
            result.put("chargeFee", chargingRecord.getChargingFee());
            result.put("serviceFee", chargingRecord.getServiceFee());
            result.put("subtotalFee", chargingRecord.getChargingFee() + chargingRecord.getServiceFee());
            
            return result;
        }
        
        return null;
    }
} 