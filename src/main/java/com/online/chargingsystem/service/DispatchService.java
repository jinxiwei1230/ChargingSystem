package com.online.chargingsystem.service;

public interface DispatchService {
    Object priorityDispatch(String carId);
    Object timeOrderDispatch(String carId);
    Object faultRecover(String pileId);
    Object singleDispatch(String carId);
    Object batchDispatch(String[] carIds);
} 