package com.online.chargingsystem.mapper;

import com.online.chargingsystem.entity.ChargingRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ChargingRecordMapper {
    @Select("SELECT * FROM charging_records WHERE id = #{id}")
    ChargingRecord findById(Long id);

    @Select("SELECT * FROM charging_records WHERE car_id = #{carId} AND start_time >= #{startTime} AND start_time < #{endTime}")
    List<ChargingRecord> findByCarIdAndDateRange(String carId, LocalDateTime startTime, LocalDateTime endTime);
} 