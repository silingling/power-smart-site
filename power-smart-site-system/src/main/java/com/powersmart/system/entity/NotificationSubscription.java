package com.powersmart.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知订阅 — 用户对某类业务通知的渠道偏好设置
 */
@Data
@TableName("notification_subscription")
public class NotificationSubscription {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID */
    private Long userId;

    /** 业务类型（hazard_approval/device_alarm/work_order/ai_detection） */
    private String bizType;

    /** 订阅渠道列表（JSON 数组，如 ["in_app","sms","email"]） */
    private String channels;

    /** 最低通知级别（info/warning/critical），默认 info */
    private String minLevel;

    /** 静默期（分钟，同类型通知在此间隔内不重复推送） */
    private Integer quietPeriodMinutes;

    /** 是否启用（1=启用，0=禁用），默认 1 */
    private Integer enabled;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updatedAt;
}
