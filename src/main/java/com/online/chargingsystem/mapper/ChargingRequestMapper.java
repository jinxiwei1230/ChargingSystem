package com.online.chargingsystem.mapper;

import com.online.chargingsystem.entity.ChargingRequest;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ChargingRequestMapper {
    @Select("SELECT * FROM charging_requests WHERE status = #{status}")
    List<ChargingRequest> findByStatus(String status);

    @Select("SELECT * FROM charging_requests WHERE request_mode = #{mode} AND status = #{status}")
    List<ChargingRequest> findByRequestModeAndStatus(String mode, String status);

    @Select("SELECT * FROM charging_requests WHERE car_id = #{carId} AND status = #{status}")
    ChargingRequest findByCarIdAndStatus(String carId, String status);

    @Update("UPDATE charging_requests SET pile_id = #{pileId}, status = #{status} WHERE id = #{id}")
    int update(ChargingRequest request);
} 