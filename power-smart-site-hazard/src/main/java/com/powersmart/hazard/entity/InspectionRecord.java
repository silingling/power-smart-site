package com.powersmart.hazard.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("inspection_record")
public class InspectionRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private Integer pointIndex;
    private String pointName;
    private String checkItem;
    private String checkResult;
    private String actualValue;
    private String description;
    private String photoJson;
    private Long issueId;
    private Long inspectorId;
    private String inspectorName;
    private LocalDateTime checkTime;
    @TableLogic
    private Integer isDeleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
