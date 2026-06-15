package com.powersmart.hazard.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("hazard_work_order")
public class HazardWorkOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long hazardId;
    private Long assigneeId;
    private Long assigneeTeamId;
    private LocalDateTime deadline;
    private String rectificationNote;
    private String rectificationImages;   // JSON数组
    private LocalDateTime rectificationTime;
    private Long verifiedBy;
    private String verifiedNote;
    private LocalDateTime verifiedTime;
    private Integer status;               // 1-待整改 2-已整改待验收 3-验收通过 4-退回重改
    private Boolean escalated;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
