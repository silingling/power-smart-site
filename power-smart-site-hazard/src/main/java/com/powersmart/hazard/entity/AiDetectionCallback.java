package com.powersmart.hazard.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * AI 检测回调记录
 */
@Data
@TableName("ai_detection_callback")
public class AiDetectionCallback {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String callbackId;
    private Long projectId;
    private Long cameraId;
    private String violationType;       // no_helmet / no_harness / zone_intrusion / ...
    private Double confidence;          // 置信度 0-1
    private String snapshotUrl;
    private String videoUrl;
    private String callbackRaw;         // 原始回调 JSON
    private String matchedRule;         // 匹配规则描述
    private Boolean processed;          // 是否已处理
    private Long hazardId;              // 关联的隐患 ID
    private String errorMsg;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
