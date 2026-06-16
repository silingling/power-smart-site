package com.powersmart.hazard.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName("emergency_drill")
public class EmergencyDrill {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private Long planId;

    private String drillName;

    private String drillType;

    private LocalDate drillDate;

    private LocalTime drillTime;

    private Integer durationMinutes;

    private String location;

    private Integer participantsCount;

    private String organizer;

    private String content;

    private String evaluation;

    private String deficiencies;

    private String improvementMeasures;

    private String attachmentJson;

    private String result;

    private String createdBy;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
