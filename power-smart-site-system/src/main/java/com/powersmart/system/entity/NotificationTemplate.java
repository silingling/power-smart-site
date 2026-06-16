package com.powersmart.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知模板 — 定义各种业务通知的消息模板
 */
@Data
@TableName("notification_template")
public class NotificationTemplate {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 模板名称 */
    private String templateName;

    /** 模板唯一标识 */
    private String templateKey;

    /** 业务类型（hazard_approval/device_alarm/work_order/ai_detection） */
    private String bizType;

    /** 渠道（in_app/sms/email/feishu） */
    private String channel;

    /** 标题模板，支持 {var} 占位符 */
    private String titleTemplate;

    /** 内容模板，支持 {var} 占位符 */
    private String contentTemplate;

    /** 模板变量列表（JSON） */
    private String variables;

    /** 是否启用（1=启用，0=禁用） */
    private Integer enabled;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updatedAt;
}
