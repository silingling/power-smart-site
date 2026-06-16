package com.powersmart.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知渠道配置 — 各第三方渠道的系统级参数
 */
@Data
@TableName("notification_channel_config")
public class NotificationChannelConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 渠道名称（in_app/sms/email/feishu） */
    private String channel;

    /** 配置键 */
    private String configKey;

    /** 配置值 */
    private String configValue;

    /** 是否启用（1=启用，0=禁用），默认 1 */
    private Integer enabled;

    /** 配置说明 */
    private String description;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updatedAt;
}
