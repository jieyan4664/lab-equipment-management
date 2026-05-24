package com.lab.backed.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 预约实体
 */
@Data
@TableName("reservation")
public class Reservation implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    @TableField("student_id")
    private Integer studentId;
    
    @TableField("device_id")
    private Integer deviceId;
    
    @TableField("start_time")
    private LocalDateTime startTime;
    
    @TableField("end_time")
    private LocalDateTime endTime;
    
    private String purpose;
    
    private String status;
    
    private String reason;
    
    @TableField("teacher_id")
    private Integer teacherId;
    
    @TableField("audit_time")
    private LocalDateTime auditTime;
    
    @TableField("created_at")
    private LocalDateTime createdAt;
    
    @TableField("updated_at")
    private LocalDateTime updatedAt;
    
    // 非数据库字段
    @TableField(exist = false)
    private String deviceName;
    
    @TableField(exist = false)
    private String deviceCode;
    
    @TableField(exist = false)
    private String studentName;
    
    @TableField(exist = false)
    private String studentNo;
}
