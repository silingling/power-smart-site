package com.powersmart.device.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 安全围栏/电子围栏
 *
 * <p>支持两种形状：
 * <ul>
 *   <li><b>圆形</b> — centerLat / centerLng / radiusM</li>
 *   <li><b>多边形</b> — polygonPoints (JSON: [[lng,lat],[lng,lat],...])</li>
 * </ul>
 * fenceType: circle / polygon
 */
@Data
@TableName("safety_fence")
public class SafetyFence {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private String fenceName;
    private String fenceType;           // circle / polygon
    private String color;               // 围栏显示颜色 #RRGGBB
    private String description;

    // ── 圆形参数 ──
    private BigDecimal centerLat;
    private BigDecimal centerLng;
    private BigDecimal radiusM;

    // ── 多边形参数 ──
    private String polygonPoints;       // JSON: [[lng,lat],[lng,lat],...]

    private String alertLevel;          // high / medium / low
    private Integer enabled;            // 1=启用 0=禁用

    private String createBy;
    @TableLogic
    private Integer isDeleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
