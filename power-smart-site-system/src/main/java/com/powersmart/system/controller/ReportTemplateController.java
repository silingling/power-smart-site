package com.powersmart.system.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.annotation.OperateLog;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.common.util.PageHelper;
import com.powersmart.system.entity.ReportTemplate;
import com.powersmart.system.service.ReportExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 报表模板管理接口 — /build/reportTemplate/*
 *
 * <p>提供报表模板的增删改查功能。</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/build/reportTemplate")
public class ReportTemplateController {

    private final ReportExportService exportService;

    /**
     * 分页查询模板列表
     */
    @PostMapping("/list")
    public Result<PageResult<Map<String, Object>>> list(@RequestBody(required = false) Map<String, Object> params) {
        String module = params != null ? getParamStr(params, "module") : null;
        String templateName = params != null ? getParamStr(params, "templateName") : null;
        String templateKey = params != null ? getParamStr(params, "templateKey") : null;

        Page<ReportTemplate> pageParam = PageHelper.of(params);
        LambdaQueryWrapper<ReportTemplate> wrapper = new LambdaQueryWrapper<>();

        if (StrUtil.isNotBlank(module)) {
            wrapper.eq(ReportTemplate::getModule, module);
        }
        if (StrUtil.isNotBlank(templateName)) {
            wrapper.like(ReportTemplate::getTemplateName, templateName);
        }
        if (StrUtil.isNotBlank(templateKey)) {
            wrapper.eq(ReportTemplate::getTemplateKey, templateKey);
        }
        wrapper.orderByAsc(ReportTemplate::getModule, ReportTemplate::getId);

        Page<ReportTemplate> page = exportService.listAllTemplates(wrapper, pageParam);
        List<Map<String, Object>> list = page.getRecords().stream().map(this::toMap).collect(Collectors.toList());
        return Result.ok(PageResult.of(list, page.getTotal(), (int) page.getCurrent(), (int) page.getSize()));
    }

    /**
     * 获取模板详情
     */
    @PostMapping("/get/{id}")
    public Result<Map<String, Object>> get(@PathVariable Long id) {
        ReportTemplate template = exportService.getTemplate(id);
        if (template == null) {
            return Result.fail("报表模板不存在");
        }
        return Result.ok(toMap(template));
    }

    /**
     * 新增模板
     */
    @PostMapping("/add")
    @OperateLog(module = "报表模板", action = "add", description = "新增报表模板")
    public Result<Void> add(@RequestBody ReportTemplate template) {
        if (StrUtil.isBlank(template.getTemplateName())) {
            return Result.fail("templateName 不能为空");
        }
        if (StrUtil.isBlank(template.getTemplateKey())) {
            return Result.fail("templateKey 不能为空");
        }
        if (StrUtil.isBlank(template.getModule())) {
            return Result.fail("module 不能为空");
        }

        exportService.addTemplate(template);
        return Result.ok();
    }

    /**
     * 更新模板
     */
    @PostMapping("/set")
    @OperateLog(module = "报表模板", action = "update", description = "更新报表模板")
    public Result<Void> set(@RequestBody ReportTemplate template) {
        if (template.getId() == null) {
            return Result.fail("id 不能为空");
        }
        ReportTemplate existing = exportService.getTemplate(template.getId());
        if (existing == null) {
            return Result.fail("报表模板不存在");
        }

        // 只更新非空字段
        if (StrUtil.isNotBlank(template.getTemplateName())) {
            existing.setTemplateName(template.getTemplateName());
        }
        if (StrUtil.isNotBlank(template.getTemplateKey())) {
            existing.setTemplateKey(template.getTemplateKey());
        }
        if (StrUtil.isNotBlank(template.getDescription())) {
            existing.setDescription(template.getDescription());
        }
        if (StrUtil.isNotBlank(template.getModule())) {
            existing.setModule(template.getModule());
        }
        if (StrUtil.isNotBlank(template.getExportType())) {
            existing.setExportType(template.getExportType());
        }
        if (StrUtil.isNotBlank(template.getColumnsConfig())) {
            existing.setColumnsConfig(template.getColumnsConfig());
        }
        if (StrUtil.isNotBlank(template.getQueryConfig())) {
            existing.setQueryConfig(template.getQueryConfig());
        }
        if (StrUtil.isNotBlank(template.getHeaderTemplate())) {
            existing.setHeaderTemplate(template.getHeaderTemplate());
        }
        if (StrUtil.isNotBlank(template.getFooterTemplate())) {
            existing.setFooterTemplate(template.getFooterTemplate());
        }
        if (template.getEnabled() != null) {
            existing.setEnabled(template.getEnabled());
        }
        if (StrUtil.isNotBlank(template.getCreatedBy())) {
            existing.setCreatedBy(template.getCreatedBy());
        }

        exportService.updateTemplate(existing);
        return Result.ok();
    }

    /**
     * 删除模板
     */
    @PostMapping("/del/{id}")
    @OperateLog(module = "报表模板", action = "delete", description = "删除报表模板")
    public Result<Void> del(@PathVariable Long id) {
        ReportTemplate existing = exportService.getTemplate(id);
        if (existing == null) {
            return Result.fail("报表模板不存在");
        }
        exportService.deleteTemplate(id);
        return Result.ok();
    }

    // ==================== 转换方法 ====================

    private Map<String, Object> toMap(ReportTemplate t) {
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

    private String getParamStr(Map<String, Object> params, String key) {
        if (params == null || !params.containsKey(key)) return null;
        Object val = params.get(key);
        return val != null ? val.toString() : null;
    }
}
