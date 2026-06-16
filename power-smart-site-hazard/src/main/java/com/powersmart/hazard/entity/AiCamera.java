package com.powersmart.hazard.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * AI 摄像头
 */
@Data
@TableName("ai_camera")
public class AiCamera {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private String cameraName;
    private String cameraCode;
    private String cameraType;          // ptz / fixed / dome
    private String streamUrl;           // 直播流地址
    private String snapshotUrl;         // 静态截图地址
    private String longitude;           // 经度
    private String latitude;            // 纬度
    private String locationDesc;        // 位置描述
    private String direction;           // 朝向
    private Boolean aiEnabled;          // AI 识别开关
    private String detectionZones;      // 检测区域 JSON
    private Integer status;             // 0-离线 1-在线
    private LocalDateTime lastHeartbeat;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
