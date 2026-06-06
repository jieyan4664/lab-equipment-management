package com.lab.backed.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lab.backed.entity.SystemSetting;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统设置Mapper
 */
@Mapper
public interface SystemSettingMapper extends BaseMapper<SystemSetting> {
}
