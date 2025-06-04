package com.online.chargingSystem.mapper;

import com.online.chargingSystem.entity.ChargingPile;
import com.online.chargingSystem.entity.enums.ChargingPileStatus;
import com.online.chargingSystem.entity.enums.ChargingPileType;
import com.online.chargingSystem.dto.ChargingPileQueueDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface ChargingPileMapper {
    // 插入充电桩
    int insert(ChargingPile pile);
    
    // 更新充电桩
    int update(ChargingPile pile);
    
    // 根据ID查询充电桩
    ChargingPile findById(@Param("pileId") String pileId);
    
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
     * 根据状态查询充电桩
     * @param status 充电桩状态
     * @return 充电桩列表
     */
    @Select("SELECT * FROM charging_pile WHERE status = #{status}")
    List<ChargingPile> findByStatus(@Param("status") ChargingPileStatus status);
    
    /**
     * 根据类型和状态查询充电桩
     * @param type 充电桩类型
     * @param status 充电桩状态
     * @return 充电桩列表
     */
    @Select("SELECT * FROM charging_pile WHERE type = #{type} AND status = #{status}")
    List<ChargingPile> findByTypeAndStatus(@Param("type") ChargingPileType type, @Param("status") ChargingPileStatus status);
    
    // 将请求添加到充电桩队列
    void addToQueue(@Param("pileId") String pileId, @Param("requestId") Long requestId);

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