package com.online.chargingSystem.service.impl;

import com.online.chargingSystem.entity.ChargingRequest;
import com.online.chargingSystem.mapper.ChargingRequestMapper;
import com.online.chargingSystem.service.ChargingRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChargingRequestServiceImpl implements ChargingRequestService {

    @Autowired
    private ChargingRequestMapper chargingRequestMapper;

    @Override
    public List<ChargingRequest> findAll() {
        return chargingRequestMapper.findAll();
    }
} 