package com.powersmart.worker.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("worker")
public class Worker {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private Long teamId;
    private String name;
    private String idCard;
    private String phone;
    private String avatarUrl;
    private LocalDate entryDate;
    private LocalDate exitDate;

    @TableLogic
    private Integer status;       // 1-在岗 0-退场
    private String workerType;
    private Boolean trainingPassed;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
