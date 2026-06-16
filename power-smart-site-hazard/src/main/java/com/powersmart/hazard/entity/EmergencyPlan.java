package com.powersmart.hazard.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("emergency_plan")
public class EmergencyPlan {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private String planName;

    private String planType;

    private String emergencyLevel;

    private String description;

    private String procedures;

    private String responsiblePerson;

    private String responsiblePhone;

    private String responsibleDept;

    private Integer drillRequired;

    private String drillFrequency;

    private String attachmentJson;

    private String status;

    private String createdBy;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
