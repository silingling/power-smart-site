package com.powersmart.hazard.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("emergency_incident")
public class EmergencyIncident {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private String incidentCode;

    private String incidentName;

    private String incidentType;

    private String incidentLevel;

    private LocalDateTime happenedAt;

    private String location;

    private String description;

    private Integer casualties;

    private Integer deaths;

    private BigDecimal directLoss;

    private BigDecimal indirectLoss;

    private String preliminaryCause;

    private String investigationReport;

    private String correctiveActions;

    private String correctiveStatus;

    private String reporter;

    private LocalDateTime reportTime;

    private String attachmentJson;

    private String status;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
