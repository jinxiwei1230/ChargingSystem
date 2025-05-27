package com.online.chargingSystem.mapper;

import com.online.chargingSystem.entity.ChargingDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ChargingDetailMapper {
    // 插入充电明细记录
    int insert(ChargingDetail detail);
    
    // 批量插入充电明细记录
    int batchInsert(@Param("details") List<ChargingDetail> details);
    
    // 根据订单ID查询充电明细
    List<ChargingDetail> findByOrderId(@Param("orderId") String orderId);
    
    // 根据用户ID和日期查询充电明细
    List<ChargingDetail> findByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    // 根据充电桩ID和日期查询充电明细
    List<ChargingDetail> findByPileIdAndDate(@Param("pileId") String pileId, @Param("date") LocalDate date);
    
    // 更新充电明细状态
    int updateStatus(@Param("id") Long id, @Param("status") String status);
    
    // 更新充电明细金额
    int updateAmount(@Param("id") Long id, @Param("amount") Double amount);
} 