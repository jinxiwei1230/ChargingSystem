package com.online.chargingSystem.service;

import com.online.chargingSystem.entity.ChargingPile;
import com.online.chargingSystem.entity.enums.ChargingPileStatus;

public interface ChargingPileService {
    // 获取充电桩状态
    ChargingPile getPileStatus(String pileId);

    // 报告故障
    boolean reportFault(String pileId, String faultType, String description);

    // 解决故障
    boolean resolveFault(String pileId, String resolution);

    // 开启充电桩
    boolean powerOn(String pileId);

    // 设置充电桩参数
    boolean setParameters(String pileId, Double chargingPower);

    // 启动充电桩
    boolean startChargingPile(String pileId);

    // 关闭充电桩
    boolean powerOff(String pileId);

    // 查询充电桩状态
    ChargingPile queryPileState(String pileId);
} 