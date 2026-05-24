package com.lab.backed.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lab.backed.entity.Violation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 违规记录Mapper
 */
@Mapper
public interface ViolationMapper extends BaseMapper<Violation> {
}
