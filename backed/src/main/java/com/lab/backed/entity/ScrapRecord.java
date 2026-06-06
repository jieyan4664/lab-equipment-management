package com.lab.backed.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 报废记录实体
 */
@Data
@TableName("scrap_record")
public class ScrapRecord {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    /**
     * 设备ID（外键）
     */
    private Integer deviceId;
    
    /**
     * 报废日期
     */
    private LocalDate scrapDate;
    
    /**
     * 原因：wear/damage/obsolete/other
     */
    private String reason;
    
    /**
     * 详细说明
     */
    private String description;
    
    /**
     * 处置：keep/discard/recycle
     */
    private String disposal;
    
    /**
     * 登记老师ID
     */
    private Integer teacherId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
