package com.powersmart.hazard.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 安全资料变更日志（对应tongye build/safetyMaterialChangelog）
 */
@Data
@TableName("safety_material_changelog")
public class SafetyMaterialChangelog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long materialId;
    private Long projectId;
    private String changeType;
    private String changeContent;
    private String operator;
    private Integer isDeleted;
    private LocalDateTime createTime;
}
