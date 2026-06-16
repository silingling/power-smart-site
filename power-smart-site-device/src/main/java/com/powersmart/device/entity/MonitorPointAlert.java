package com.powersmart.device.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 监测点告警（对应tongye build/monitorPointAlert）
 */
@Data
@TableName("monitor_point_alert")
public class MonitorPointAlert {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private Long deviceAssetId;
    private String pointType;
    private String alertContent;
    private String alertLevel;
    private java.math.BigDecimal alertValue;
    private java.math.BigDecimal thresholdValue;
    private Integer status;
    private LocalDateTime handleTime;
    private String handleBy;
    private String handleRemark;
    private Integer isDeleted;
    private LocalDateTime createTime;
}
