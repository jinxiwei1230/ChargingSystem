package com.online.chargingSystem.mapper;

import com.online.chargingSystem.entity.ChargingOrder;
import com.online.chargingSystem.entity.enums.OrderStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ChargingOrderMapper {
    /**
     * 插入充电订单
     * @param order 充电订单
     * @return 影响的行数
     */
    int insert(ChargingOrder order);
    
    /**
     * 更新订单记录
     * @param order 订单信息
     * @return 更新的记录数
     */
    int update(ChargingOrder order);
    
    /**
     * 根据订单ID查询充电订单
     * @param orderId 订单ID
     * @return 充电订单
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
     * 根据请求ID查询充电订单
     * @param requestId 请求ID
     * @return 充电订单
     */
    ChargingOrder findByRequestId(@Param("requestId") String requestId);
    
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

    /**
     * 统计用户订单数量
     * @param userId 用户ID
     * @param status 订单状态
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 订单数量
     */
    int countUserOrders(@Param("userId") Long userId,
                       @Param("status") String status,
                       @Param("startDate") LocalDate startDate,
                       @Param("endDate") LocalDate endDate);

    /**
     * 查询用户订单列表
     * @param userId 用户ID
     * @param status 订单状态
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param offset 偏移量
     * @param size 每页条数
     * @return 订单列表
     */
    List<ChargingOrder> findUserOrders(@Param("userId") Long userId,
                                      @Param("status") String status,
                                      @Param("startDate") LocalDate startDate,
                                      @Param("endDate") LocalDate endDate,
                                      @Param("offset") int offset,
                                      @Param("size") int size);

    /**
     * 查询充电桩订单列表
     * @param pileId 充电桩ID
     * @param date 日期
     * @param status 订单状态
     * @return 订单列表
     */
    List<ChargingOrder> findPileOrders(@Param("pileId") String pileId,
                                      @Param("date") LocalDate date,
                                      @Param("status") String status);

    /**
     * eenndd
     * 根据请求ID和状态查询订单
     * @param requestId 请求ID
     * @param status 订单状态
     * @return 充电订单
     */
    ChargingOrder findByRequestIdAndStatus(@Param("requestId") Long requestId, @Param("status") OrderStatus status);

    /**
     * 根据请求ID查找订单
     * @param requestId 充电请求ID
     * @return 充电订单
     */
    ChargingOrder findByRequestId(Long requestId);
} 