package com.lab.backed.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 维修记录实体
 */
@Data
@TableName("repair_record")
public class RepairRecord {
    
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
     * 维修日期
     */
    private LocalDate repairDate;
    
    /**
     * 维修人员
     */
    private String repairPerson;
    
    /**
     * 维修费用
     */
    private BigDecimal cost;
    
    /**
     * 结果：repaired/unrepairable
     */
    private String result;
    
    /**
     * 维修说明
     */
    private String description;
    
    /**
     * 维修凭证图片（JSON数组）
     */
    private String images;
    
    /**
     * 登记老师ID
     */
    private Integer teacherId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
