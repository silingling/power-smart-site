package com.powersmart.hazard.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("inspection_task")
public class InspectionTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private Long planId;
    private Long templateId;
    private String taskCode;
    private String taskName;
    private Long assigneeId;
    private String assigneeName;
    private LocalDate scheduledDate;
    private LocalDateTime deadline;
    private LocalDateTime completedAt;
    private Integer totalPoints;
    private Integer completedPoints;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String result;
    private String remark;
    @TableLogic
    private Integer isDeleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
