package com.powersmart.progress.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("safety_log")
public class SafetyLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private LocalDate logDate;
    private String weather;
    private String safetyEducation;
    private String hazardCheck;
    private String rectificationStatus;
    private String violations;
    private String safetyMeasures;
    private String recorder;
    private String recorderName;
    private Long signatoryId;
    private String signatoryName;
    private LocalDateTime signedAt;
    private String status;
    @TableLogic
    private Integer isDeleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
