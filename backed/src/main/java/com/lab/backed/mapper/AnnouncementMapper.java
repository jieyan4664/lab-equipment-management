package com.lab.backed.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lab.backed.entity.Announcement;
import org.apache.ibatis.annotations.Mapper;

/**
 * 公告Mapper
 */
@Mapper
public interface AnnouncementMapper extends BaseMapper<Announcement> {
}
