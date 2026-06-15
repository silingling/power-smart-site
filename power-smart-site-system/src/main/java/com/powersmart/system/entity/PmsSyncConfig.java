package com.powersmart.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * PMS 同步配置 — 连接配置 + 同步范围 + 调度策略
 */
@Data
@TableName("pms_sync_config")
public class PmsSyncConfig {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String configKey;               // pms_api_url / pms_app_id / pms_secret / sync_interval
    private String configValue;
    private String description;
    private Integer enabled;                // 1-启用 0-禁用
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
