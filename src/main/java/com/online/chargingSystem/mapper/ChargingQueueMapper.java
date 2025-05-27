package com.online.chargingSystem.mapper;

import com.online.chargingSystem.entity.ChargingQueue;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ChargingQueueMapper {
    ChargingQueue findByPileId(String pileId);

    List<ChargingQueue> findByQueueType(String queueType);

    List<ChargingQueue> findByCarId(String carId);

    List<ChargingQueue> findByQueueTypeAndStatus(String queueType, String status);

    ChargingQueue findById(Long id);

    int insert(ChargingQueue queue);

    // 更新队列记录
    int update(ChargingQueue queue);

    // 根据请求ID查询队列记录
    ChargingQueue findByRequestId(Long requestId);

    // 查询等待中的队列记录
    List<ChargingQueue> findWaitingQueues();

    // 更新队列状态
    int updateStatus(Long id, String status);

    // 删除队列记录
    int delete(Long id);
} 