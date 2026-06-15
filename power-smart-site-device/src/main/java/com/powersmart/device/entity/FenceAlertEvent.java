package com.powersmart.device.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 围栏告警事件
 *
 * <p>当人员/设备定位数据进入或离开围栏范围时产生。
 * eventType: enter / leave</p>
 */
@Data
@TableName("fence_alert_event")
public class FenceAlertEvent {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long fenceId;
    private Long projectId;
    private String eventType;           // enter / leave
    private String targetType;          // person / device / vehicle
    private String targetId;            // 人员ID / 设备编号
    private String targetName;          // 人员姓名 / 设备名称
    private BigDecimal eventLat;
    private BigDecimal eventLng;
    private String description;
    private String status;              // pending / processed / ignored
    private String processedBy;
    private LocalDateTime processedAt;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
