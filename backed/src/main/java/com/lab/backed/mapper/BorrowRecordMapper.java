package com.lab.backed.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lab.backed.entity.BorrowRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 借用记录Mapper
 */
@Mapper
public interface BorrowRecordMapper extends BaseMapper<BorrowRecord> {
}
