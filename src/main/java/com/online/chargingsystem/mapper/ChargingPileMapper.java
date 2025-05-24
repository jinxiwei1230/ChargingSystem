package com.online.chargingsystem.mapper;

import com.online.chargingsystem.entity.ChargingPile;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ChargingPileMapper {
    @Select("SELECT * FROM charging_piles WHERE pile_id = #{pileId}")
    ChargingPile findByPileId(String pileId);

    @Select("SELECT * FROM charging_piles WHERE type = #{type} AND status = #{status}")
    List<ChargingPile> findByTypeAndStatus(String type, String status);

    @Update("UPDATE charging_piles SET status = #{status} WHERE pile_id = #{pileId}")
    int update(ChargingPile pile);
} 