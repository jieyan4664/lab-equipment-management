package com.lab.backed.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 设备实体
 */
@Data
@TableName("device")
public class Device implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    private String code;
    
    private String name;
    
    @TableField("category_id")
    private Integer categoryId;
    
    private String brand;
    
    private String model;
    
    private String spec;
    
    @TableField("technical_params")
    private String technicalParams;
    
    private String location;
    
    @TableField("purchase_date")
    private LocalDate purchaseDate;
    
    @TableField("warranty_date")
    private LocalDate warrantyDate;
    
    private String status;
    
    @TableField("current_borrower_id")
    private Integer currentBorrowerId;
    
    @TableField("expected_return_time")
    private LocalDateTime expectedReturnTime;
    
    private String description;
    
    @TableField("qr_code")
    private String qrCode;
    
    @TableField("created_at")
    private LocalDateTime createdAt;
    
    @TableField("updated_at")
    private LocalDateTime updatedAt;
    
    @TableField(exist = false)
    private String categoryName;
    
    @TableField(exist = false)
    private String thumbnail;
    
    @TableField(exist = false)
    private Boolean isFavorited;
}
