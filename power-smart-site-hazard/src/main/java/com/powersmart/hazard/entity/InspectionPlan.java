package com.powersmart.hazard.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("inspection_plan")
public class InspectionPlan {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private String planName;
    private String planType;
    private String routeName;
    private String routeJson;
    private Integer totalPoints;
    private String frequency;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long assignedTo;
    private String assigneeName;
    private String description;
    private String status;
    @TableLogic
    private Integer isDeleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
