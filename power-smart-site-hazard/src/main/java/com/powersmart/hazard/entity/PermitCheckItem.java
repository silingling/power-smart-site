package com.powersmart.hazard.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 作业票检查项模板 — 按作业票类型分类的安全检查项
 */
@Data
@TableName("permit_check_item")
public class PermitCheckItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String permitType;               // hot_work/height_work/...
    private String itemName;                 // 检查项名称
    private String itemCategory;             // 分类: person/equipment/environment/measure
    private Integer required;                // 1-必选 0-可选
    private Integer sortOrder;
    private Integer enabled;                 // 1-启用 0-禁用
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private java.time.LocalDateTime createdAt;
}
