package com.online.chargingSystem.mapper;

import com.online.chargingSystem.entity.ChargingPile;
import com.online.chargingSystem.dto.ChargingPileQueueDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ChargingPileMapper {
    // 插入充电桩
    int insert(ChargingPile pile);
    
    // 更新充电桩
    int update(ChargingPile pile);
    
    // 根据ID查询充电桩
    ChargingPile findById(@Param("pileId") String pileId);
    
    // 根据类型和状态查询充电桩
    List<ChargingPile> findByTypeAndStatus(@Param("type") String type, @Param("status") String status);
    
    // 查询所有充电桩
    List<ChargingPile> findAll();
    
    // 查询可用充电桩
    List<ChargingPile> findAvailable();
    
    // 查询故障充电桩
    List<ChargingPile> findFaulty();
    
    // 更新充电桩状态
    int updateStatus(@Param("pileId") String pileId, @Param("status") String status);
    
    // 更新充电桩参数
    int updateParameters(@Param("pileId") String pileId, @Param("chargingPower") Double chargingPower);

    /**
     * 查询指定充电桩的等候队列信息
     * @param pileId 充电桩ID
     * @return 等候队列信息列表
     */
    List<ChargingPileQueueDTO> findPileQueueInfo(@Param("pileId") String pileId);

    /**
     * 查询所有充电桩的等候队列信息
     * @return 所有充电桩的等候队列信息
     */
    List<ChargingPileQueueDTO> findAllPileQueueInfo();
} 