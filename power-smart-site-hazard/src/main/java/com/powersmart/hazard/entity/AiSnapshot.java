package com.powersmart.hazard.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * AI 抓拍快照
 */
@Data
@TableName("ai_snapshot")
public class AiSnapshot {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long cameraId;
    private Long projectId;
    private String snapshotUrl;
    private LocalDateTime snapshotTime;
    private String thumbnailUrl;
    private Long fileSize;
    private Integer width;
    private Integer height;
    private String storageType;         // local / oss / cos
    private LocalDateTime expiredAt;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
