package com.lab.backed.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lab.backed.entity.ScrapRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 报废记录Mapper
 */
@Mapper
public interface ScrapRecordMapper extends BaseMapper<ScrapRecord> {
}
