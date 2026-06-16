package com.powersmart.hazard.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("inspection_template")
public class InspectionTemplate {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private String templateName;
    private String templateType;
    private String checkItems;
    private Integer enabled;
    @TableLogic
    private Integer isDeleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
