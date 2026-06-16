package com.powersmart.progress.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("electronic_signature")
public class ElectronicSignature {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private String bizType;
    private Long bizId;
    private Long signerId;
    private String signerName;
    private String signerRole;
    private LocalDateTime signedAt;
    private String signatureImage;
    private String ipAddress;
    private String userAgent;
    private String remark;
    private LocalDateTime createdAt;
}
