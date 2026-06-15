package com.powersmart.worker.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 工人进出场记录
 */
@Data
@TableName("labour_advance_retreat")
public class LabourAdvanceRetreat {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long workerId;
    private Long projectId;
    private String workerName;
    private String idCard;             // 身份证号
    private String phone;
    private Long teamId;
    private Long subcontractorId;
    private String worktype;           // 工种
    private LocalDate entryDate;       // 进场日期
    private LocalDate exitDate;        // 退场日期
    private String entryType;          // 入场方式：首次/返场
    private String exitType;           // 退场方式：正常/辞退/调离
    private String remark;
    private Integer status;            // 1-在场 0-已退场
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
