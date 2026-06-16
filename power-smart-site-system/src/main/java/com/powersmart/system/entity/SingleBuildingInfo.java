package com.powersmart.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * 单体楼栋信息（对应tongye build/singleBuildingInfo）
 */
@Data
@TableName("single_building_info")
public class SingleBuildingInfo {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private String buildingName;
    private String buildingCode;
    private String buildingType;
    private Integer totalFloors;
    private java.math.BigDecimal totalArea;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer status;
    private String remark;
    private Integer isDeleted;
    private String createBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
