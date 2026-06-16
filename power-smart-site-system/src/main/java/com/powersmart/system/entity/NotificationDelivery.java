package com.powersmart.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知投递记录 — 每次渠道推送的明细与状态
 */
@Data
@TableName("notification_delivery")
public class NotificationDelivery {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联通知 ID（站内通知记录） */
    private Long notificationId;

    /** 目标用户 ID */
    private Long userId;

    /** 渠道（in_app/sms/email/feishu） */
    private String channel;

    /** 投递状态（pending/sent/failed） */
    private String status;

    /** 发送时间 */
    private LocalDateTime sentAt;

    /** 错误信息 */
    private String errorMsg;

    /** 重试次数 */
    private Integer retryCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
