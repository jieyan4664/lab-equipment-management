package com.lab.backed.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lab.backed.entity.DeviceCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 设备分类Mapper
 */
@Mapper
public interface DeviceCategoryMapper extends BaseMapper<DeviceCategory> {
}
