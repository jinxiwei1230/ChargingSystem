package com.online.chargingSystem.service;

import com.online.chargingSystem.entity.ChargingRequest;
import java.util.List;

public interface DispatchService {
    // 优先级调度
    boolean priorityDispatch(Long requestId);
    
    // 时间顺序调度
    boolean timeOrderDispatch(Long requestId);
    
    // 故障恢复调度
    boolean faultRecover(String pileId);
    
    // 单次调度
    List<ChargingRequest> singleDispatch(List<Long> requestIds);
    
    // 批量调度
    List<ChargingRequest> batchDispatch(List<Long> requestIds);
    
    // 获取最优调度方案
    String getOptimalPile(Long requestId);
} 