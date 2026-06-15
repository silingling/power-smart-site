package com.powersmart.device.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * 设备资产（对应萤丰 build/equipmentAssets）
 */
@Data
@TableName("equipment_assets")
public class EquipmentAssets {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String deviceName;
    private String deviceCode;
    private String deviceType;
    private Long projectId;
    private Long locationId;
    private String monitorPointType;
    private Integer status;
    private Long videoMonitorId;
    private String brand;
    private String model;
    private LocalDate installDate;
    private String remark;
    private Integer isDeleted;
    private String createBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
