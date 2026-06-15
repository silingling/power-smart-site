package com.powersmart.worker.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("worker_certificate")
public class WorkerCertificate {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long workerId;
    private String certType;
    private String certNumber;
    private LocalDate issueDate;
    private LocalDate expireDate;
    private String issueAuthority;
    private String certImageUrl;
    private Boolean verified;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
