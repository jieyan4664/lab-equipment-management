package com.lab.backed.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lab.backed.entity.Device;
import org.apache.ibatis.annotations.Mapper;

/**
 * 设备Mapper
 */
@Mapper
public interface DeviceMapper extends BaseMapper<Device> {
}
