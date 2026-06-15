package com.powersmart.device.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 变电站巡检记录
 */
@Data
@TableName("substation_inspection")
public class SubstationInspection {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private Long equipmentId;
    private String inspector;
    private String inspectionType;       // routine/patrol/special
    private LocalDate inspectionDate;
    private String content;              // 检测内容JSON

    private BigDecimal sf6Pressure;
    private BigDecimal temperature;
    private BigDecimal noiseDb;
    private BigDecimal vibrationMm;
    private String result;               // normal/abnormal/urgent
    private String description;
    private String imageJson;            // 现场照片JSON数组

    @TableLogic
    private Integer isDeleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
