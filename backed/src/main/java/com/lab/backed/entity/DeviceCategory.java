package com.lab.backed.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 设备分类实体
 */
@Data
@TableName("device_category")
public class DeviceCategory implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    private String name;
    
    @TableField("parent_id")
    private Integer parentId;
    
    @TableField("lab_type")
    private String labType;
    
    @TableField("sort_order")
    private Integer sortOrder;
    
    private Integer status;
    
    @TableField("created_at")
    private LocalDateTime createdAt;
}
