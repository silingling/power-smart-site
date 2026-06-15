package com.powersmart.hazard.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 隐患审批流程
 */
@Data
@TableName("hazard_approval")
public class HazardApproval {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long hazardId;
    private Integer currentNode;         // 当前审批节点序号
    private Integer totalNodes;          // 总节点数
    private String approvalStatus;       // pending/approving/approved/rejected/escalated
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private Integer escalated;           // 0-否 1-是
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
