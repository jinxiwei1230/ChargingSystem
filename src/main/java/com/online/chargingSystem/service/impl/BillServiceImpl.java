package com.online.chargingSystem.service.impl;

import com.online.chargingSystem.entity.ChargingOrder;
import com.online.chargingSystem.entity.ChargingDetail;
import com.online.chargingSystem.entity.enums.PeriodType;
import com.online.chargingSystem.service.BillService;
import com.online.chargingSystem.mapper.ChargingDetailMapper;
import com.online.chargingSystem.mapper.ChargingOrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.ArrayList;

@Service
public class BillServiceImpl implements BillService {

    private static final BigDecimal SERVICE_RATE = new BigDecimal("0.80");
    
    @Autowired
    private ChargingDetailMapper chargingDetailMapper;
    
    @Autowired
    private ChargingOrderMapper chargingOrderMapper;

    @Override
    public ChargingOrder getBillInfo(String billId) {
        // TODO: 实现获取账单信息逻辑
        return null;
    }

    @Override
    @Transactional
    public ChargingOrder requestBill(String carId, String requestMode) {
        // TODO: 实现请求账单逻辑
        return null;
    }

    @Override
    public ChargingOrder getOrderInfo(String orderId) {
        return chargingOrderMapper.findById(orderId);
    }

    @Override
    public List<ChargingDetail> getOrderDetails(String orderId) {
        return chargingDetailMapper.findByOrderId(orderId);
    }

    @Override
    public List<ChargingOrder> getUserOrders(Long userId, LocalDate date) {
        return chargingOrderMapper.findByUserIdAndDate(userId, date);
    }

    @Override
    @Transactional
    public ChargingOrder generateOrder(Long requestId) {
        // TODO: 实现生成订单逻辑
        return null;
    }

    @Override
    @Transactional
    public void calculateFees(String orderId) {
        // 1. 获取订单信息
        ChargingOrder order = getOrderInfo(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在：" + orderId);
        }
        
        // 2. 删除旧的详单记录
        chargingDetailMapper.deleteByOrderId(orderId);
        
        // 3. 计算并生成新的详单记录
        List<ChargingDetail> details = calculateChargingDetails(
            orderId,
            order.getStartTime(),
            order.getEndTime(),
            order.getTotalKwh(),
            order.getChargingPower()
        );
        
        // 4. 批量插入详单记录
        if (!details.isEmpty()) {
            chargingDetailMapper.batchInsert(details);
        }
        
        // 5. 更新订单总金额
        BigDecimal totalChargeFee = details.stream()
            .map(ChargingDetail::getChargeFee)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        BigDecimal totalServiceFee = details.stream()
            .map(ChargingDetail::getServiceFee)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        order.setTotalChargeFee(totalChargeFee);
        order.setTotalServiceFee(totalServiceFee);
        order.setTotalFee(totalChargeFee.add(totalServiceFee));
        
        // 6. 更新订单信息
        chargingOrderMapper.update(order);
    }
    
    @Override
    public List<ChargingDetail> calculateChargingDetails(
            String orderId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            BigDecimal totalKwh,
            BigDecimal chargingPower) {
        
        List<ChargingDetail> details = new ArrayList<>();
        int periodSeq = 1;
        
        // 计算总时长（小时）
        BigDecimal totalDuration = totalKwh.divide(chargingPower, 2, RoundingMode.HALF_UP);
        
        // 获取开始和结束时间的小时部分
        LocalTime currentTime = startTime.toLocalTime();
        LocalTime endTimeOfDay = endTime.toLocalTime();
        
        // 计算每个时段的充电量
        BigDecimal remainingKwh = totalKwh;
        LocalDateTime currentStartTime = startTime;
        
        while (currentTime.isBefore(endTimeOfDay) && remainingKwh.compareTo(BigDecimal.ZERO) > 0) {
            // 获取当前时段的结束时间
            LocalDateTime periodEndTime = getNextPeriodStartTime(currentTime);
            if (periodEndTime.isAfter(endTime)) {
                periodEndTime = endTime;
            }
            
            // 计算当前时段的时长（小时）
            BigDecimal periodDuration = BigDecimal.valueOf(
                java.time.Duration.between(currentStartTime, periodEndTime).toMinutes()
            ).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
            
            // 计算当前时段的充电量
            BigDecimal periodKwh = periodDuration.multiply(chargingPower);
            if (periodKwh.compareTo(remainingKwh) > 0) {
                periodKwh = remainingKwh;
            }
            
            // 获取当前时段的电价
            BigDecimal chargeRate = getChargeRate(currentTime);
            
            // 创建详单记录
            ChargingDetail detail = new ChargingDetail();
            detail.setOrderId(orderId);
            detail.setPeriodSeq(periodSeq++);
            detail.setPeriodType(getPeriodType(currentTime));
            detail.setStartTime(currentStartTime);
            detail.setEndTime(periodEndTime);
            detail.setDuration(periodDuration);
            detail.setKwh(periodKwh);
            detail.setChargeRate(chargeRate);
            detail.setServiceRate(SERVICE_RATE);
            
            // 计算费用
            detail.setChargeFee(periodKwh.multiply(chargeRate));
            detail.setServiceFee(periodKwh.multiply(SERVICE_RATE));
            detail.setSubTotal(detail.getChargeFee().add(detail.getServiceFee()));
            
            details.add(detail);
            
            // 更新剩余充电量和时间
            remainingKwh = remainingKwh.subtract(periodKwh);
            currentStartTime = periodEndTime;
            currentTime = periodEndTime.toLocalTime();
        }
        
        return details;
    }
    
    private LocalDateTime getNextPeriodStartTime(LocalTime currentTime) {
        if (currentTime.isBefore(LocalTime.of(7, 0))) {
            return LocalDateTime.now().with(LocalTime.of(7, 0));
        } else if (currentTime.isBefore(LocalTime.of(10, 0))) {
            return LocalDateTime.now().with(LocalTime.of(10, 0));
        } else if (currentTime.isBefore(LocalTime.of(15, 0))) {
            return LocalDateTime.now().with(LocalTime.of(15, 0));
        } else if (currentTime.isBefore(LocalTime.of(18, 0))) {
            return LocalDateTime.now().with(LocalTime.of(18, 0));
        } else if (currentTime.isBefore(LocalTime.of(21, 0))) {
            return LocalDateTime.now().with(LocalTime.of(21, 0));
        } else if (currentTime.isBefore(LocalTime.of(23, 0))) {
            return LocalDateTime.now().with(LocalTime.of(23, 0));
        } else {
            return LocalDateTime.now().plusDays(1).with(LocalTime.of(7, 0));
        }
    }
    
    private BigDecimal getChargeRate(LocalTime time) {
        if (time.isAfter(LocalTime.of(10, 0)) && time.isBefore(LocalTime.of(15, 0)) ||
            time.isAfter(LocalTime.of(18, 0)) && time.isBefore(LocalTime.of(21, 0))) {
            return new BigDecimal("1.00"); // 峰时
        } else if (time.isAfter(LocalTime.of(7, 0)) && time.isBefore(LocalTime.of(10, 0)) ||
                   time.isAfter(LocalTime.of(15, 0)) && time.isBefore(LocalTime.of(18, 0)) ||
                   time.isAfter(LocalTime.of(21, 0)) && time.isBefore(LocalTime.of(23, 0))) {
            return new BigDecimal("0.70"); // 平时
        } else {
            return new BigDecimal("0.40"); // 谷时
        }
    }
    
    private PeriodType getPeriodType(LocalTime time) {
        if (time.isAfter(LocalTime.of(10, 0)) && time.isBefore(LocalTime.of(15, 0)) ||
            time.isAfter(LocalTime.of(18, 0)) && time.isBefore(LocalTime.of(21, 0))) {
            return PeriodType.PEAK;
        } else if (time.isAfter(LocalTime.of(7, 0)) && time.isBefore(LocalTime.of(10, 0)) ||
                   time.isAfter(LocalTime.of(15, 0)) && time.isBefore(LocalTime.of(18, 0)) ||
                   time.isAfter(LocalTime.of(21, 0)) && time.isBefore(LocalTime.of(23, 0))) {
            return PeriodType.STANDARD;
        } else {
            return PeriodType.VALLEY;
        }
    }
} 