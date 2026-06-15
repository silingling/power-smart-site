package com.powersmart.worker.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 分包商（劳务公司/施工单位）
 */
@Data
@TableName("labour_subcontractor")
public class LabourSubcontractor {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private String subcontractorName;     // 分包商名称
    private String subcontractorType;     // 劳务分包/专业分包/总包
    private String legalPerson;           // 法人代表
    private String contactPhone;
    private String creditCode;            // 统一社会信用代码
    private String businessLicense;       // 营业执照URL
    private String qualificationLevel;    // 资质等级
    private Integer workerCount;          // 在册人数
    private String contractUrl;           // 合同文件URL
    private Integer status;               // 1-合作中 0-已终止
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
