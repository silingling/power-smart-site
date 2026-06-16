package com.powersmart.hazard.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 特种作业票 — 动火/高处/受限空间/临时用电/吊装/动土/断路
 *
 * <p>状态机:</p>
 * <pre>
 * draft ⇢ submitted ⇢ safety_review ⇢ approved(已签发) ⇢ active(进行中) ⇢ completed(完工) ⇢ closed(已归档)
 *   ↓           ↓             ↓
 * cancelled   rejected      rejected
 * </pre>
 */
@Data
@TableName("special_work_permit")
public class SpecialWorkPermit {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private String permitNo;                 // 票号: SP-2026-06-15-001
    private String permitType;               // hot_work/height_work/confined_space/temp_electric/lifting/excavation/road_blockage

    // ── 基本信息 ──
    private String title;
    private String workLocation;
    private String workContent;
    private String riskAnalysis;             // 风险辨识
    private String safetyMeasures;           // 安全措施 JSON [{name, done}]
    private String gasTestResult;            // 气体检测结果 JSON (受限空间/动火)
    private LocalDateTime expectedStartTime;
    private LocalDateTime expectedEndTime;
    private String workTeamName;             // 作业班组
    private Integer workerCount;             // 作业人数

    // ── 申请人/监护人 ──
    private Long applicantId;
    private String applicantName;
    private String applicantDept;
    private Long guardianId;                // 监护人ID
    private String guardianName;             // 监护人姓名
    private Long principalId;                // 负责人ID
    private String principalName;            // 负责人姓名

    // ── 审批流 ──
    private String status;                   // draft/submitted/safety_review/approved/active/completed/closed/rejected/cancelled
    private String currentNode;              // 当前审批节点
    private String rejectReason;

    // ── 签发 ──
    private Long issuerId;                   // 签发人(安全负责人)
    private String issuerName;
    private LocalDateTime issueTime;

    // ── 完工 ──
    private LocalDateTime actualEndTime;
    private String completionNote;
    private Long closerId;
    private String closerName;
    private LocalDateTime closeTime;

    // ── 延期 ──
    private Integer extendedCount;           // 延期次数
    private LocalDateTime newEndTime;        // 延期后的结束时间
    private String extensionReason;

    // ── 签名 ──
    private String applicantSignature;       // 申请人签名(base64)
    private String issuerSignature;          // 签发人签名
    private String closerSignature;          // 完工确认人签名

    // ── 附件 ──
    private String attachmentJson;           // JSON数组

    private String remark;
    private String createBy;
    @TableLogic
    private Integer isDeleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
