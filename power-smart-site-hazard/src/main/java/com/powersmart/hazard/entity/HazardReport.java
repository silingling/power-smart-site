package com.powersmart.hazard.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("hazard_report")
public class HazardReport {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private Integer reportType;       // 1-AI 2-人工
    private String hazardType;
    private Integer hazardLevel;      // 1-一般 2-较大 3-重大
    private String description;
    private String location;
    private Long areaId;
    private String imageUrl;
    private String videoUrl;
    private Long reportedBy;
    private Integer status;           // 1-待整改 2-整改中 3-已验收 4-已归档
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
