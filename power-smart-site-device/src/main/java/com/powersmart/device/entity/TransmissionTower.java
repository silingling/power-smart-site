package com.powersmart.device.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 输电线路杆塔台账
 */
@Data
@TableName("transmission_tower")
public class TransmissionTower {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private String towerCode;
    private String towerName;
    private String towerType;           // angle/tension/suspension/terminal/transition
    private String voltageLevel;

    private BigDecimal heightM;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal altitudeM;
    private String foundationType;
    private BigDecimal foundationDepthM;
    private Integer legCount;

    private String manufacturer;
    private String model;
    private String serialNumber;
    private LocalDate manufactureDate;
    private LocalDate installDate;
    private Integer designLifeYears;
    private String status;

    private LocalDate lastInspectionDate;
    private LocalDate nextInspectionDate;
    private String imageJson;
    private String remark;

    @TableLogic
    private Integer isDeleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
