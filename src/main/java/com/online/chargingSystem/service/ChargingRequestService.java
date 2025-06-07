package com.online.chargingSystem.service;

import com.online.chargingSystem.entity.ChargingRequest;
import java.util.List;

public interface ChargingRequestService {
    List<ChargingRequest> findAll();
} 