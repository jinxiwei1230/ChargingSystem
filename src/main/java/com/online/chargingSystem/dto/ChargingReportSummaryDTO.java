package com.online.chargingSystem.dto;

import lombok.Data;
import java.util.List;

@Data
public class ChargingReportSummaryDTO {
    private ChargingReportDTO summary;           // 总体汇总数据
    private List<ChargingReportDTO> pileDetails; // 每个充电桩的详细数据
} 