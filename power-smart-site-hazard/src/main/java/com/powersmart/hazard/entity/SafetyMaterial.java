package com.powersmart.hazard.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 安全资料（对应萤丰 build/safetyMaterial）
 */
@Data
@TableName("safety_material")
public class SafetyMaterial {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private Long catalogId;            // 分类目录ID
    private String title;
    private String fileUrl;
    private String fileType;           // pdf/doc/jpg
    private Long uploadBy;
    private Integer isCollect;         // 0-未收藏 1-已收藏
    private Integer isQual;            // 0-安全资料 1-质量资料
    private Integer status;            // 1-正常 0-删除
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
