package com.powersmart.hazard.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 审批节点配置
 */
@Data
@TableName("approval_node")
public class ApprovalNode {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String bizType;
    private String nodeName;
    private Integer nodeOrder;
    private String roleKey;
    private String nodeAction;
    private Integer timeoutHours;
    private String autoAction;           // escalate/pass/reject
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
