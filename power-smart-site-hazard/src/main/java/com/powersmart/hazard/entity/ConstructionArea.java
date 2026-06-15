package com.powersmart.hazard.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 作业区域（含电子围栏定义）
 */
@Data
@TableName("construction_area")
public class ConstructionArea {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private String areaName;
    private String areaType;       // 高压禁区/吊装区/临时带电区/一般作业区
    private String riskLevel;      // high / medium / low
    private String fencePoints;    // 围栏坐标点JSON [{lng,lat}...]
    private Long responsiblePersonId;
    private Long responsibleTeamId;
    private Integer status;        // 1-启用 0-禁用
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
