package com.online.chargingSystem.service;

import com.online.chargingSystem.entity.ChargingPile;
import com.online.chargingSystem.entity.enums.ChargingPileStatus;
import com.online.chargingSystem.dto.ChargingPileQueueDTO;
import com.online.chargingSystem.dto.ChargingReportDTO;
import com.online.chargingSystem.dto.ChargingReportSummaryDTO;
import com.online.chargingSystem.dto.ChargingQueueInfoDTO;
import java.util.List;
import java.util.Map;

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

    /**
     * 获取指定充电桩的等候队列信息
     * @param pileId 充电桩ID
     * @return 等候队列信息列表
     */
    List<ChargingPileQueueDTO> getPileQueueInfo(String pileId);

    /**
     * 获取所有充电桩的等候队列信息
     * @return 所有充电桩的等候队列信息
     */
    Map<String, List<ChargingPileQueueDTO>> getAllPileQueueInfo();

    /**
     * 开始充电
     * @param requestId 充电请求ID
     * @param pileId 充电桩ID
     * @return 是否成功开始充电
     */
    boolean startCharging(Long requestId, String pileId);

    /**
     * 结束充电
     * @param userId 用户ID
     * @param pileId 充电桩ID
     * @return 操作结果
     */
    boolean endCharging(Long userId, String pileId);

    /**
     * 获取充电桩报表数据
     * @param pileId 充电桩ID（可选）
     * @param timeType 时间类型（day/week/month）
     * @return 报表数据列表
     */
    List<ChargingReportDTO> getChargingReport(String pileId, String timeType);

    /**
     * 获取所有充电桩的汇总报表数据
     * @param timeType 时间类型（day/week/month）
     * @return 汇总报表数据（包含总体数据和每个充电桩的详细数据）
     */
    ChargingReportSummaryDTO getChargingReportSummary(String timeType);

    /**
     * 获取充电桩等候队列详细信息
     * @param pileId 充电桩ID
     * @return 等候队列详细信息列表
     */
    List<ChargingQueueInfoDTO> getChargingQueueDetails(String pileId);

    List<ChargingPile> findAll();
} 