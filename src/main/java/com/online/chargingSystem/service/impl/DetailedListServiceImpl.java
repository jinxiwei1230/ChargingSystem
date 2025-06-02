package com.online.chargingSystem.service.impl;

import com.online.chargingSystem.entity.ChargingDetail;
import com.online.chargingSystem.mapper.ChargingDetailMapper;
import com.online.chargingSystem.service.DetailedListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
public class DetailedListServiceImpl implements DetailedListService {

    @Autowired
    private ChargingDetailMapper chargingDetailMapper;

    @Override
    public List<ChargingDetail> getOrderDetails(String orderId) {
        return chargingDetailMapper.findByOrderId(orderId);
    }

    @Override
    public List<ChargingDetail> getUserDailyDetails(Long userId, LocalDate date) {
        return chargingDetailMapper.findByUserIdAndDate(userId, date);
    }

    @Override
    public List<ChargingDetail> getUserChargingDetails(Long userId) {
        return chargingDetailMapper.findByUserId(userId);
    }
} 