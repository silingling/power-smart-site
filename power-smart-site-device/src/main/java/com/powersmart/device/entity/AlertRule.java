package com.powersmart.device.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 告警规则 — 可配置的阈值规则
 */
@Data
@TableName("alert_rule")
public class AlertRule {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String ruleName;
    private String deviceType;           // 空=通用规则
    private String sensorType;           // load/tilt/temperature/pm25/noise
    private String operator;             // gt/lt/gte/lte/eq
    private java.math.BigDecimal warningThreshold;
    private java.math.BigDecimal criticalThreshold;
    private Integer durationSeconds;     // 持续超限秒数（防抖）
    private Integer enabled;
    private String remark;
    @TableLogic
    private Integer isDeleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
