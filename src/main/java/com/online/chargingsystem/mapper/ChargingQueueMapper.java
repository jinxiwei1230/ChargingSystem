package com.online.chargingsystem.mapper;

import com.online.chargingsystem.entity.ChargingQueue;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ChargingQueueMapper {
    @Select("SELECT * FROM charging_queues WHERE pile_id = #{pileId}")
    ChargingQueue findByPileId(String pileId);

    @Select("SELECT * FROM charging_queues WHERE queue_type = #{queueType}")
    List<ChargingQueue> findByQueueType(String queueType);

    @Select("SELECT * FROM charging_queues WHERE car_id = #{carId}")
    List<ChargingQueue> findByCarId(String carId);

    @Select("SELECT * FROM charging_queues WHERE queue_type = #{queueType} AND status = #{status}")
    List<ChargingQueue> findByQueueTypeAndStatus(String queueType, String status);

    @Select("SELECT * FROM charging_queues WHERE id = #{id}")
    ChargingQueue findById(Long id);

    @Insert("INSERT INTO charging_queues (car_id, request_mode, request_amount, queue_type, status, queue_time) " +
            "VALUES (#{carId}, #{requestMode}, #{requestAmount}, #{queueType}, #{status}, #{queueTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ChargingQueue queue);
} 