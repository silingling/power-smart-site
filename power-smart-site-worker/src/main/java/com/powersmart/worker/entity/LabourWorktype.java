package com.powersmart.worker.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 工种字典
 */
@Data
@TableName("labour_worktype")
public class LabourWorktype {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String worktypeName;       // 工种名称 e.g. 电工/焊工/塔吊司机
    private String worktypeCode;       // 工种编码
    private String certRequired;       // 所需证书类型
    private Integer sortOrder;
    private Integer status;            // 1-启用 0-禁用
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
