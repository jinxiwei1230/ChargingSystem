package com.online.chargingsystem.service.impl;

import com.online.chargingsystem.entity.ChargingRecord;
import com.online.chargingsystem.mapper.ChargingRecordMapper;
import com.online.chargingsystem.service.BillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BillServiceImpl implements BillService {

    @Autowired
    private ChargingRecordMapper chargingRecordMapper;

    @Override
    public Object getBillInfo(String billId) {
        // 1. 查询充电记录
        ChargingRecord record = chargingRecordMapper.findById(Long.parseLong(billId));
        if (record == null) {
            return null;
        }

        // 2. 构建账单信息
        Map<String, Object> result = new HashMap<>();
        result.put("billId", record.getId());
        result.put("carId", record.getCarId());
        result.put("pileId", record.getPileId());
        result.put("startTime", record.getStartTime());
        result.put("endTime", record.getEndTime());
        result.put("chargingAmount", record.getChargingAmount());
        result.put("chargingFee", record.getChargingFee());
        result.put("serviceFee", record.getServiceFee());
        result.put("totalFee", record.getChargingFee() + record.getServiceFee());
        
        return result;
    }

    @Override
    public Object requestBill(String carId, String date) {
        // 1. 解析日期
        LocalDate targetDate = LocalDate.parse(date);
        LocalDateTime startOfDay = targetDate.atStartOfDay();
        LocalDateTime endOfDay = targetDate.plusDays(1).atStartOfDay();

        // 2. 查询指定日期的充电记录
        List<ChargingRecord> records = chargingRecordMapper.findByCarIdAndDateRange(
            carId, startOfDay, endOfDay);

        // 3. 构建账单列表
        Map<String, Object> result = new HashMap<>();
        result.put("carId", carId);
        result.put("date", date);
        result.put("totalChargingAmount", records.stream()
            .mapToDouble(ChargingRecord::getChargingAmount)
            .sum());
        result.put("totalChargingFee", records.stream()
            .mapToDouble(ChargingRecord::getChargingFee)
            .sum());
        result.put("totalServiceFee", records.stream()
            .mapToDouble(ChargingRecord::getServiceFee)
            .sum());
        result.put("records", records);

        return result;
    }
} 