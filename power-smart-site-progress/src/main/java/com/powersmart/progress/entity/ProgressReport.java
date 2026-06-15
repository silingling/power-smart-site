package com.powersmart.progress.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("progress_report")
public class ProgressReport {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private Long reporterId;           // worker表ID
    private LocalDate reportDate;
    private BigDecimal completionRate;
    private Integer workerCount;
    private String imageUrls;          // JSON数组
    private String note;
    private String location;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
