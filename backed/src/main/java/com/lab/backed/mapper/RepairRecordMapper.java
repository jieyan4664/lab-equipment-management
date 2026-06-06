package com.lab.backed.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lab.backed.entity.RepairRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 维修记录Mapper
 */
@Mapper
public interface RepairRecordMapper extends BaseMapper<RepairRecord> {
}
