package com.powersmart.hazard.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 质量资料变更日志（对应tongye build/qualMaterialChangelog）
 */
@Data
@TableName("qual_material_changelog")
public class QualMaterialChangelog {
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
