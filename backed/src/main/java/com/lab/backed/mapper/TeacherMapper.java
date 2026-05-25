package com.lab.backed.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lab.backed.entity.Teacher;
import org.apache.ibatis.annotations.Mapper;

/**
 * 老师Mapper
 */
@Mapper
public interface TeacherMapper extends BaseMapper<Teacher> {
}
