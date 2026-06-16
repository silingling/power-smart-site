package com.powersmart.hazard.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("emergency_supply_record")
public class EmergencySupplyRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private Long supplyId;

    private String recordType;

    private Integer quantity;

    private String operator;

    private LocalDateTime operationTime;

    private String reason;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
