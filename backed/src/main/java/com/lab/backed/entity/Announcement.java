package com.lab.backed.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 公告实体
 */
@Data
@TableName("announcement")
public class Announcement implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    private String title;
    
    private String content;
    
    private String attachments;
    
    @TableField("target_type")
    private String targetType;
    
    @TableField("target_ids")
    private String targetIds;
    
    @TableField("is_pinned")
    private Integer isPinned;
    
    @TableField("publish_time")
    private LocalDateTime publishTime;
    
    @TableField("teacher_id")
    private Integer teacherId;
    
    private Integer status;
    
    @TableField("created_at")
    private LocalDateTime createdAt;
}
