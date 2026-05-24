package com.lab.backed.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lab.backed.entity.Reservation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 预约Mapper
 */
@Mapper
public interface ReservationMapper extends BaseMapper<Reservation> {
}
