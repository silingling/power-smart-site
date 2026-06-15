package com.powersmart.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * PMS ID 映射 — PMS 系统 ↔ 本地系统 ID 对应关系
 */
@Data
@TableName("pms_id_mapping")
public class PmsIdMapping {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String entityType;              // project/worker/hazard/device/...
    private String localId;                 // 本地系统ID(字符串)
    private String pmsId;                   // PMS系统ID
    private LocalDateTime lastSyncTime;
    private String syncStatus;              // synced / pending / conflict
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
