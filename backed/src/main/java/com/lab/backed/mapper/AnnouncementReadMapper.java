package com.lab.backed.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lab.backed.entity.AnnouncementRead;
import org.apache.ibatis.annotations.Mapper;

/**
 * 公告已读记录Mapper
 */
@Mapper
public interface AnnouncementReadMapper extends BaseMapper<AnnouncementRead> {
}
