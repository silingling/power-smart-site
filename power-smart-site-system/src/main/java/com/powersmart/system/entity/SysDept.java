package com.powersmart.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 部门/组织架构
 */
@Data
@TableName("sys_dept")
public class SysDept {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long parentId;
    private String deptName;
    private String deptType;          // company/department/team
    private Integer sortOrder;
    private Integer status;           // 1-启用 0-禁用
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
