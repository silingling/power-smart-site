package com.powersmart.progress.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("log_template")
public class LogTemplate {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private String templateName;
    private String logType;
    private String templateContent;
    private Integer enabled;
    @TableLogic
    private Integer isDeleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
