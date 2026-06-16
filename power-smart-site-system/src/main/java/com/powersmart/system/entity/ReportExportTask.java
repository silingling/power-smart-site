package com.powersmart.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报表导出任务 — 每次导出操作的异步任务记录
 *
 * <p>导出请求先创建任务记录（status=running），
 * 文件生成后更新为 completed，并提供下载 URL 及过期时间。</p>
 */
@Data
@TableName("report_export_task")
public class ReportExportTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联模板 ID */
    private Long templateId;

    /** 快照：导出的模板名称 */
    private String templateName;

    /** 所属项目 ID */
    private String projectId;

    /** 操作人 ID */
    private String operatorId;

    /** 操作人姓名 */
    private String operatorName;

    /** 查询参数快照（JSON） */
    private String queryParams;

    /** 导出的文件名 */
    private String fileName;

    /** 文件下载 URL/路径 */
    private String fileUrl;

    /** 文件大小（字节） */
    private Long fileSize;

    /** 任务状态：running / completed / failed */
    private String status;

    /** 错误消息 */
    private String errorMsg;

    /** 导出的数据行数 */
    private Integer rowCount;

    /** 开始时间 */
    private LocalDateTime startedAt;

    /** 完成时间 */
    private LocalDateTime completedAt;

    /** 过期时间（文件自动清理） */
    private LocalDateTime expiresAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
