package com.powersmart.progress.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("progress_task")
public class ProgressTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private Long parentId;
    private String taskName;
    private Integer taskLevel;          // 1-单位工程 2-分部 3-分项 4-工序
    private LocalDate planStartDate;
    private LocalDate planEndDate;
    private LocalDate actualStartDate;
    private BigDecimal actualCompletionRate;
    private Long responsibleTeamId;
    private String responsiblePerson;
    private String predecessorTaskIds;
    private Integer sortOrder;
    private Integer status;             // 0-未开始 1-进行中 2-已完成 3-滞后
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
