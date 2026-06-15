package com.powersmart.device.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * 变电站设备台账 — 统一表，device_type 区分 GIS/transformer/breaker
 */
@Data
@TableName("substation_equipment")
public class SubstationEquipment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private String deviceType;           // GIS / transformer / breaker
    private String deviceCode;
    private String deviceName;
    private String bayNumber;            // 间隔编号（GIS）
    private String voltageLevel;

    // 通用资产
    private String manufacturer;
    private String model;
    private String serialNumber;
    private LocalDate manufactureDate;
    private LocalDate installDate;
    private LocalDate commissionDate;
    private Integer designLifeYears;
    private String status;               // in_service / maintenance / retired / fault
    private LocalDate lastMaintenanceDate;
    private LocalDate nextMaintenanceDate;

    // ── GIS ──
    private String gasType;
    private BigDecimal sf6PressureKpa;
    private BigDecimal sf6AlarmPressureKpa;
    private Integer sealedPartsCount;

    // ── Transformer ──
    private BigDecimal ratedCapacityMva;
    private String coolingMethod;
    private String tapChangerType;
    private Integer tapChangerPositions;
    private String oilType;
    private BigDecimal oilWeightKg;
    private String windingConnection;

    // ── Breaker ──
    private BigDecimal ratedCurrentKa;
    private BigDecimal ratedVoltageKv;
    private BigDecimal ratedBreakingCurrentKa;
    private String operatingMechanism;
    private Integer operatingVoltageV;
    private Integer mechanicalOperations;
    private Integer breakingCount;

    // 位置
    private String locationDesc;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private Long parentId;
    private Long videoMonitorId;

    // 附件
    private String attachmentJson;
    private String remark;
    private String createBy;
    @TableLogic
    private Integer isDeleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
