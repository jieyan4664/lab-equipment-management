package com.lab.backed.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统设置实体
 */
@Data
@TableName("system_setting")
public class SystemSetting implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    /**
     * 设置键（唯一）
     */
    @TableField("setting_key")
    private String settingKey;
    
    /**
     * 设置值（JSON格式）
     */
    @TableField("setting_value")
    private String settingValue;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
