package com.lab.backed.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 老师实体
 */
@Data
@TableName("teacher")
public class Teacher implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    @TableField("teacher_no")
    private String teacherNo;
    
    private String name;
    
    private String phone;
    
    private String email;
    
    private String password;
    
    private String role;
    
    private Integer status;
    
    @TableField("created_at")
    private LocalDateTime createdAt;
    
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
