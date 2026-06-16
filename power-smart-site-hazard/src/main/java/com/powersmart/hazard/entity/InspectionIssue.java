package com.powersmart.hazard.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("inspection_issue")
public class InspectionIssue {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private Long recordId;
    private String issueDesc;
    private String issueType;
    private String issueLevel;
    private String photoJson;
    private String location;
    private String status;
    private Long handlerId;
    private String handlerName;
    private LocalDateTime handledAt;
    private String handleMeasure;
    private Long verifierId;
    private String verifierName;
    private LocalDateTime verifiedAt;
    private Long hazardReportId;
    @TableLogic
    private Integer isDeleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
