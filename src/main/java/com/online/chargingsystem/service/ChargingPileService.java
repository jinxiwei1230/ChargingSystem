package com.online.chargingsystem.service;

public interface ChargingPileService {
    Object getPileStatus(String pileId);
    Object reportFault(String pileId, String faultType, String description);
    Object resolveFault(String pileId, String resolution);
    Object powerOn(String pileId);
    Object setParameters(Object parameters);
    Object startChargingPile(String pileId);
    Object powerOff(String pileId);
    Object queryPileState(String pileId);
} 