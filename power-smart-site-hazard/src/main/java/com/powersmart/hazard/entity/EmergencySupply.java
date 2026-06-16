package com.powersmart.hazard.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("emergency_supply")
public class EmergencySupply {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private String supplyName;

    private String supplyType;

    private String specification;

    private String unit;

    private Integer quantity;

    private Integer minQuantity;

    private String location;

    private String storageCondition;

    private LocalDate expiryDate;

    private String supplier;

    private String contactPhone;

    private String remark;

    private String status;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
