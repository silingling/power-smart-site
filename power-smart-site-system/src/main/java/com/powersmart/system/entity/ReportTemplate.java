package com.powersmart.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报表模板 — 定义导出报表的结构、查询、样式配置
 *
 * <p>每个模板包含列配置、查询配置、页眉页脚，
 * 通过 templateKey 在业务模块中唯一标识。</p>
 */
@Data
@TableName("report_template")
public class ReportTemplate {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 模板名称（前端展示用） */
    private String templateName;

    /** 模板标识（程序内引用用，如 "project_summary"） */
    private String templateKey;

    /** 模板描述 */
    private String description;

    /** 所属模块（如 project/safety/quality/progress） */
    private String module;

    /** 导出类型：excel / csv */
    private String exportType;

    /** 列配置（JSON 数组，定义每列的 key、标题、宽度、类型） */
    private String columnsConfig;

    /** 查询配置（JSON，定义 SQL/Conditions/Params） */
    private String queryConfig;

    /** 页眉模板（可选，Excel 顶部固定行） */
    private String headerTemplate;

    /** 页脚模板（可选，Excel 底部固定行） */
    private String footerTemplate;

    /** 是否启用：1-启用 0-禁用 */
    private Integer enabled;

    /** 创建人 */
    private String createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
