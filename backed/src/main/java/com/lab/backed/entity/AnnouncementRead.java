package com.lab.backed.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 公告已读记录实体
 */
@Data
@TableName("announcement_read")
public class AnnouncementRead implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    @TableField("student_id")
    private Integer studentId;
    
    @TableField("announcement_id")
    private Integer announcementId;
    
    @TableField("is_read")
    private Integer isRead;
    
    @TableField("read_time")
    private LocalDateTime readTime;
    
    @TableField("created_at")
    private LocalDateTime createdAt;
}
