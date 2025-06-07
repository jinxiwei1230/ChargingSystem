package com.online.chargingSystem.controller;

import com.online.chargingSystem.entity.ChargingRequest;
import com.online.chargingSystem.service.ChargingRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/charging-requests")
public class ChargingRequestController {

    @Autowired
    private ChargingRequestService chargingRequestService;

    @GetMapping("/all")
    public List<ChargingRequest> getAllChargingRequests() {
        return chargingRequestService.findAll();
    }
} 