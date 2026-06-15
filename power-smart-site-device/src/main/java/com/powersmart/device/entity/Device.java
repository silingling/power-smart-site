package com.powersmart.device.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("device")
public class Device {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private String deviceCode;
    private String deviceName;
    private String deviceType;
    private String manufacturer;
    private String model;
    private String serialNumber;
    private LocalDate entryDate;
    private LocalDate nextInspectionDate;
    private LocalDate nextMaintenanceDate;
    private Integer status;           // 1-正常 2-运行 3-维修 4-退场
    private Long operatorId;
    private String location;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
