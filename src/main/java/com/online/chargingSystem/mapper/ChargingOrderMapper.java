package com.online.chargingSystem.mapper;

import com.online.chargingSystem.entity.ChargingOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ChargingOrderMapper {
    /**
     * 插入订单记录
     * @param order 订单信息
     * @return 插入的记录数
     */
    int insert(ChargingOrder order);
    
    /**
     * 更新订单记录
     * @param order 订单信息
     * @return 更新的记录数
     */
    int update(ChargingOrder order);
    
    /**
     * 根据订单ID查询订单
     * @param orderId 订单ID
     * @return 订单信息
     */
    ChargingOrder findById(@Param("orderId") String orderId);
    
    /**
     * 根据用户ID和日期查询订单列表
     * @param userId 用户ID
     * @param date 日期
     * @return 订单列表
     */
    List<ChargingOrder> findByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    /**
     * 根据请求ID查询订单
     * @param requestId 请求ID
     * @return 订单信息
     */
    ChargingOrder findByRequestId(@Param("requestId") Long requestId);
    
    /**
     * 更新订单状态
     * @param orderId 订单ID
     * @param status 状态
     * @return 更新的记录数
     */
    int updateStatus(@Param("orderId") String orderId, @Param("status") String status);
    
    /**
     * 更新订单金额
     * @param orderId 订单ID
     * @param totalChargeFee 电费
     * @param totalServiceFee 服务费
     * @param totalFee 总费用
     * @return 更新的记录数
     */
    int updateAmount(
        @Param("orderId") String orderId,
        @Param("totalChargeFee") Double totalChargeFee,
        @Param("totalServiceFee") Double totalServiceFee,
        @Param("totalFee") Double totalFee
    );
} 