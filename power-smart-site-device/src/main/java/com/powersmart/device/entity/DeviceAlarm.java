package com.powersmart.device.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 设备告警记录
 */
@Data
@TableName("device_alarm")
public class DeviceAlarm {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long deviceId;
    private String alarmType;        // 超载/超温/超幅/超时
    private String alarmLevel;       // warning / critical
    private Double alarmValue;
    private Double thresholdValue;
    private String description;
    private Integer status;          // 0-未处理 1-已处理 2-已忽略
    private Long handledBy;
    private LocalDateTime handledTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
