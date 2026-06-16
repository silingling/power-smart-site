package com.powersmart.hazard.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("emergency_contact")
public class EmergencyContact {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private String contactName;

    private String contactRole;

    private String organization;

    private String department;

    private String position;

    private String phone;

    private String landline;

    private String email;

    private String duty;

    private Integer sortOrder;

    private String status;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
