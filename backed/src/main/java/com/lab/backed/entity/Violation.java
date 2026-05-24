package com.lab.backed.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 违规记录实体
 */
@Data
@TableName("violation")
public class Violation implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    @TableField("student_id")
    private Integer studentId;
    
    @TableField("borrow_id")
    private Integer borrowId;
    
    private String type;
    
    @TableField("violation_time")
    private LocalDateTime violationTime;
    
    private String punishment;
    
    @TableField("ban_days")
    private Integer banDays;
    
    @TableField("compensation_amount")
    private BigDecimal compensationAmount;
    
    private String description;
    
    @TableField("teacher_id")
    private Integer teacherId;
    
    private Integer status;
    
    @TableField("created_at")
    private LocalDateTime createdAt;
}
