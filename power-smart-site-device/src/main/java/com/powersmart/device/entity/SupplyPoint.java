package com.powersmart.device.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * 水电供应点（对应tongye adminSupplyPoint）
 */
@Data
@TableName("supply_point")
public class SupplyPoint {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private String pointName;
    private String pointType;
    private String deviceCode;
    private String locationDesc;
    private BigDecimal unitPrice;
    private BigDecimal currentReading;
    private Integer status;
    private Integer isDeleted;
    private String createBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
