package com.online.chargingSystem.mapper;

import com.online.chargingSystem.entity.ChargingOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ChargingOrderMapper {
    // 插入订单记录
    int insert(ChargingOrder order);
    
    // 更新订单记录
    int update(ChargingOrder order);
    
    // 根据订单ID查询订单
    ChargingOrder findById(@Param("orderId") String orderId);
    
    // 根据用户ID和日期查询订单列表
    List<ChargingOrder> findByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    // 根据请求ID查询订单
    ChargingOrder findByRequestId(@Param("requestId") Long requestId);
    
    // 更新订单状态
    int updateStatus(@Param("orderId") String orderId, @Param("status") String status);
    
    // 更新订单金额
    int updateAmount(@Param("orderId") String orderId, @Param("totalAmount") Double totalAmount);
} 