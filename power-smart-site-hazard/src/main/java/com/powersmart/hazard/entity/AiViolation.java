package com.powersmart.hazard.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * AI 违规识别记录
 */
@Data
@TableName("ai_violation")
public class AiViolation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private Long cameraId;
    private String violationType;      // no_helmet / no_harness / zone_intrusion / ...
    private Double confidence;         // 识别置信度 0-1
    private String snapshotUrl;        // 抓拍图片URL
    private String videoUrl;           // 短视频URL
    private Long workerId;             // 识别到的人员ID（如有）
    private Integer status;            // 0-未处理 1-已确认 2-误报
    private Long handledBy;
    private LocalDateTime handledTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
