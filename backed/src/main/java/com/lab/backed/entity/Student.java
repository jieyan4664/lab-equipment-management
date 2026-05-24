package com.lab.backed.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 学生实体
 */
@Data
@TableName("student")
public class Student implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    @TableField("student_no")
    private String studentNo;
    
    private String name;
    
    @TableField("class_name")
    private String className;
    
    private String phone;
    
    private String email;
    
    private String password;
    
    @TableField("lab_type")
    private String labType;
    
    @TableField("access_status")
    private Integer accessStatus;
    
    @TableField("access_expire")
    private LocalDate accessExpire;
    
    @TableField("violation_count")
    private Integer violationCount;
    
    private Integer status;
    
    @TableField("created_at")
    private LocalDateTime createdAt;
    
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
