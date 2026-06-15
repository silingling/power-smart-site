package com.powersmart.device.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 视频监控摄像头
 */
@Data
@TableName("video_monitor")
public class VideoMonitor {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private Long locationId;                // 关联 equipment_location
    private String cameraName;
    private String cameraCode;
    private String cameraType;              // 球机/枪机/全景
    private String vendor;                  // 萤石/海康/大华
    private String deviceSerial;            // 设备序列号
    private String validateCode;            // 验证码
    private String streamUrl;               // RTSP/HLS 地址
    private String installPosition;         // 安装位置描述
    private String aiFunctions;             // AI功能：安全帽检测/区域入侵等
    private Integer status;                 // 1-在线 0-离线 2-故障
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
