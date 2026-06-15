package com.powersmart.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * PMS 同步日志 — 每次同步操作的审计记录
 */
@Data
@TableName("pms_sync_log")
public class PmsSyncLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String entityType;              // project/worker/hazard/progress/device/permit/fence
    private String syncDirection;           // pull(拉取) / push(推送)
    private String action;                  // sync_all / sync_by_id / sync_by_time
    private String entityIds;               // 关联的本地ID(JSON数组)
    private String pmsIds;                  // 关联的PMS ID(JSON数组)
    private Integer totalCount;             // 本次同步总数
    private Integer successCount;           // 成功数
    private Integer failCount;              // 失败数
    private String status;                  // running / success / partial / failed
    private String errorMessage;
    private String resultJson;              // 同步结果详情(JSON)
    private Long durationMs;                // 耗时(毫秒)
    private String triggeredBy;             // manual / scheduled
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
