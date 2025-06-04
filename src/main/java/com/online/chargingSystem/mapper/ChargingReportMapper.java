package com.online.chargingSystem.mapper;

import com.online.chargingSystem.dto.ChargingReportDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ChargingReportMapper {
    
    /**
     * 获取指定充电桩在指定时间范围内的充电报表数据
     */
    List<ChargingReportDTO> getChargingReportByPileId(
        @Param("pileId") String pileId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    /**
     * 获取所有充电桩在指定时间范围内的汇总报表数据
     */
    ChargingReportDTO getChargingReportSummary(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
} 