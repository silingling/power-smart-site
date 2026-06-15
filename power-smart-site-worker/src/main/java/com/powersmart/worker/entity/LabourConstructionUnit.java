package com.powersmart.worker.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 建设单位（甲方）
 */
@Data
@TableName("labour_construction_unit")
public class LabourConstructionUnit {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private String unitName;
    private String contactPerson;
    private String contactPhone;
    private String creditCode;
    private Integer status;               // 1-启用 0-禁用
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
