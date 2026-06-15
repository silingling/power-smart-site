package com.powersmart.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作审计日志
 *
 * <p>AOP 自动记录所有 Controller 写操作，支持按用户/时间/操作类型回查。</p>
 */
@Data
@TableName("operate_log")
public class OperateLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 功能模块 */
    private String module;

    /** 操作类型（insert / update / delete / export / login / audit / other） */
    private String action;

    /** 操作描述 */
    private String description;

    /** 操作人 ID */
    private Long operatorId;

    /** 操作人姓名 */
    private String operatorName;

    /** 操作对象类型 */
    private String targetType;

    /** 操作对象 ID */
    private String targetId;

    /** 请求参数（JSON） */
    private String requestParams;

    /** 执行结果（JSON） */
    private String result;

    /** 请求 IP */
    private String ip;

    /** User-Agent */
    private String userAgent;

    /** 执行耗时 ms */
    private Integer durationMs;

    /** 1-成功 0-失败 */
    private Integer status;

    /** 错误信息 */
    private String errorMsg;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
