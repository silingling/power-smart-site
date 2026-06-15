package com.powersmart.device.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 输电线路档距/弧垂
 */
@Data
@TableName("transmission_span")
public class TransmissionSpan {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private String spanCode;
    private Long fromTowerId;
    private Long toTowerId;

    private BigDecimal spanLengthM;
    private String conductorType;
    private String conductorSpec;
    private Integer circuitCount;

    private BigDecimal designSagM;
    private BigDecimal currentSagM;
    private BigDecimal maxSagAllowedM;
    private BigDecimal sagAlarmThresholdPct;
    private BigDecimal maxWindSpeedMs;
    private BigDecimal minClearanceM;

    private String terrainType;
    private String crossingDesc;
    private LocalDate lastInspectionDate;
    private LocalDate nextInspectionDate;
    private String status;

    @TableLogic
    private Integer isDeleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
