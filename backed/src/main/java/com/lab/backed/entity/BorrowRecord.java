package com.lab.backed.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 借用记录实体
 */
@Data
@TableName("borrow_record")
public class BorrowRecord implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    @TableField("student_id")
    private Integer studentId;
    
    @TableField("device_id")
    private Integer deviceId;
    
    @TableField("teacher_id")
    private Integer teacherId;
    
    @TableField("borrow_time")
    private LocalDateTime borrowTime;
    
    @TableField("due_time")
    private LocalDateTime dueTime;
    
    @TableField("return_time")
    private LocalDateTime returnTime;
    
    private String status;
    
    @TableField("equipment_condition")
    private String equipmentCondition;
    
    @TableField("is_overdue")
    private Integer isOverdue;
    
    private String remark;
    
    @TableField("created_at")
    private LocalDateTime createdAt;
    
    @TableField("updated_at")
    private LocalDateTime updatedAt;
    
    // 非数据库字段（用于前端展示）
    @TableField(exist = false)
    private String deviceName;
    
    @TableField(exist = false)
    private String deviceCode;
    
    @TableField(exist = false)
    private String studentName;
    
    @TableField(exist = false)
    private String studentNo;
    
    @TableField(exist = false)
    private Integer remainingDays;
    
    @TableField(exist = false)
    private Integer overdueDays;
    
    @TableField(exist = false)
    private String returnCode;
    
    @TableField(exist = false)
    private Violation violation;
}
