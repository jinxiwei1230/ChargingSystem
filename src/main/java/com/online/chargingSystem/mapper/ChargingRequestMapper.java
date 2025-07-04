package com.online.chargingSystem.mapper;

import com.online.chargingSystem.entity.ChargingRequest;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ChargingRequestMapper {
    List<ChargingRequest> findByStatus(String status);

    List<ChargingRequest> findByRequestModeAndStatus(String mode, String status);

    ChargingRequest findByCarIdAndStatus(String carId, String status);

    // 更新，返回影响的行数
    int update(ChargingRequest request);

    // 插入充电请求
    int insert(ChargingRequest request);
    
    // 根据ID查询充电请求
    ChargingRequest findById(@Param("id") Long id);
    
    // 根据车辆ID查询充电请求
    ChargingRequest findByCarId(@Param("carId") String carId);
    
    // 查询用户的所有充电请求
    List<ChargingRequest> findByUserId(@Param("userId") Long userId);
    
    // 查询待处理的充电请求
    List<ChargingRequest> findPendingRequests();
    
    // 查询正在充电的请求
    List<ChargingRequest> findChargingRequests();

    // 根据充电桩ID查找请求
    List<ChargingRequest> findByPileId(@Param("pileId") String pileId);

    // 根据用户ID和状态查找充电请求
    ChargingRequest findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);

    // 查询所有充电请求
    List<ChargingRequest> findAll();
} 