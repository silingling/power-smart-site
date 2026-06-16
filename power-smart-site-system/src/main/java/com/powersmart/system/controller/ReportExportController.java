package com.powersmart.system.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.annotation.OperateLog;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.system.entity.ReportExportTask;
import com.powersmart.system.entity.ReportTemplate;
import com.powersmart.system.service.ReportExportService;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 报表导出接口 — /build/reportExport/*
 *
 * <p>提供模板列表查询、导出触发、任务查询、文件下载功能。</p>
 */
@RestController
@RequiredArgsConstructor
public class ReportExportController {

    private final ReportExportService exportService;

    /**
     * 获取可用报表模板（按模块筛选）
     */
    @PostMapping("/listTemplates")
    public Result<List<Map<String, Object>>> listTemplates(@RequestBody(required = false) Map<String, Object> params) {
        String module = params != null ? getParamStr(params, "module") : null;
        List<ReportTemplate> templates = exportService.listTemplates(module);
        List<Map<String, Object>> list = templates.stream().map(this::toTemplateMap).collect(Collectors.toList());
        return Result.ok(list);
    }

    /**
     * 获取模板详情
     */
    @PostMapping("/getTemplate/{id}")
    public Result<Map<String, Object>> getTemplate(@PathVariable Long id) {
        ReportTemplate template = exportService.getTemplate(id);
        if (template == null) {
            return Result.fail("报表模板不存在");
        }
        return Result.ok(toTemplateMap(template));
    }

    /**
     * 触发报表导出
     *
     * @param params { templateId, projectId, operatorId, operatorName, ...查询参数 }
     * @return { taskId }
     */
    @PostMapping("/export")
    @OperateLog(module = "报表导出", action = "export", description = "触发报表导出", recordResult = false)
    public Result<Map<String, Object>> export(@RequestBody Map<String, Object> params) {
        Long templateId = params.containsKey("templateId")
                ? Long.parseLong(params.get("templateId").toString()) : null;
        String projectId = getParamStr(params, "projectId");

        if (templateId == null) {
            return Result.fail("templateId 不能为空");
        }

        // 提取查询参数（排除系统参数）
        Map<String, Object> queryParams = new LinkedHashMap<>(params);
        queryParams.remove("templateId");
        queryParams.remove("projectId");
        queryParams.remove("operatorId");
        queryParams.remove("operatorName");

        try {
            Long taskId = exportService.exportExcel(templateId, projectId, params);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("taskId", taskId);
            result.put("status", "running");
            return Result.ok(result);
        } catch (Exception e) {
            return Result.fail("导出失败: " + e.getMessage());
        }
    }

    /**
     * 查询导出任务历史
     */
    @PostMapping("/taskList")
    public Result<PageResult<Map<String, Object>>> taskList(@RequestBody(required = false) Map<String, Object> params) {
        String operatorId = params != null ? getParamStr(params, "operatorId") : null;
        String status = params != null ? getParamStr(params, "status") : null;

        Page<ReportExportTask> page = exportService.getTaskHistory(operatorId, status, params);
        List<Map<String, Object>> list = page.getRecords().stream().map(this::toTaskMap).collect(Collectors.toList());
        return Result.ok(PageResult.of(list, page.getTotal(), (int) page.getCurrent(), (int) page.getSize()));
    }

    /**
     * 获取任务详情
     */
    @PostMapping("/getTask/{id}")
    public Result<Map<String, Object>> getTask(@PathVariable Long id) {
        ReportExportTask task = exportService.getTask(id);
        if (task == null) {
            return Result.fail("导出任务不存在");
        }
        return Result.ok(toTaskMap(task));
    }

    /**
     * 文件下载 — 流式返回 Excel 文件
     */
    @GetMapping("/build/reportExport/download/{taskId}")
    public void download(@PathVariable Long taskId, HttpServletResponse response) {
        ReportExportTask task = exportService.getTask(taskId);
        if (task == null || StrUtil.isBlank(task.getFileUrl())) {
            response.setStatus(404);
            return;
        }

        Path filePath = Paths.get(task.getFileUrl());
        if (!Files.exists(filePath)) {
            response.setStatus(404);
            return;
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
                "attachment; filename=" + URLEncoder.encode(task.getFileName(), StandardCharsets.UTF_8));
        response.setContentLengthLong(task.getFileSize() != null ? task.getFileSize() : 0);

        try (InputStream is = Files.newInputStream(filePath);
             ServletOutputStream os = response.getOutputStream()) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = is.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            os.flush();
        } catch (IOException e) {
            // 写响应时若连接断开则静默处理，不影响已写部分
        }
    }

    // ==================== 转换方法 ====================

    private Map<String, Object> toTemplateMap(ReportTemplate t) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", t.getId());
        m.put("templateName", t.getTemplateName());
        m.put("templateKey", t.getTemplateKey());
        m.put("description", t.getDescription());
        m.put("module", t.getModule());
        m.put("exportType", t.getExportType());
        m.put("columnsConfig", t.getColumnsConfig());
        m.put("queryConfig", t.getQueryConfig());
        m.put("headerTemplate", t.getHeaderTemplate());
        m.put("footerTemplate", t.getFooterTemplate());
        m.put("enabled", t.getEnabled());
        m.put("createdBy", t.getCreatedBy());
        m.put("createdAt", t.getCreatedAt() != null ? t.getCreatedAt().toString() : "");
        m.put("updatedAt", t.getUpdatedAt() != null ? t.getUpdatedAt().toString() : "");
        return m;
    }

    private Map<String, Object> toTaskMap(ReportExportTask t) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", t.getId());
        m.put("templateId", t.getTemplateId());
        m.put("templateName", t.getTemplateName());
        m.put("projectId", t.getProjectId());
        m.put("operatorId", t.getOperatorId());
        m.put("operatorName", t.getOperatorName());
        m.put("queryParams", t.getQueryParams());
        m.put("fileName", t.getFileName());
        m.put("fileUrl", t.getFileUrl());
        m.put("fileSize", t.getFileSize());
        m.put("status", t.getStatus());
        m.put("errorMsg", t.getErrorMsg());
        m.put("rowCount", t.getRowCount());
        m.put("startedAt", t.getStartedAt() != null ? t.getStartedAt().toString() : "");
        m.put("completedAt", t.getCompletedAt() != null ? t.getCompletedAt().toString() : "");
        m.put("expiresAt", t.getExpiresAt() != null ? t.getExpiresAt().toString() : "");
        m.put("createdAt", t.getCreatedAt() != null ? t.getCreatedAt().toString() : "");
        return m;
    }

    private String getParamStr(Map<String, Object> params, String key) {
        if (params == null || !params.containsKey(key)) return null;
        Object val = params.get(key);
        return val != null ? val.toString() : null;
    }
}
