package com.powersmart.hazard.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.annotation.OperateLog;
import com.powersmart.common.auth.SecurityContext;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.common.util.PageHelper;
import com.powersmart.hazard.entity.*;
import com.powersmart.hazard.service.ApprovalService;
import com.powersmart.hazard.service.HazardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 隐患上报管理 — /build/hazardReport/*
 */
@RestController
@RequestMapping("/build/hazardReport")
@RequiredArgsConstructor
public class HazardBuildController {

    private final HazardService hazardService;
    private final ApprovalService approvalService;

    @PostMapping("/addHazardReport")
    @OperateLog(module = "隐患管理", action = "insert", description = "上报隐患", recordResult = false)
    public Result<Map<String, Object>> addHazardReport(@RequestBody Map<String, Object> params) {
        HazardReport report = new HazardReport();
        report.setProjectId(safeLong(params.get("projectId")));
        report.setReportType(safeInt(params.get("reportType"), 2));
        report.setHazardType(params.getOrDefault("hazardType", "其他").toString());
        report.setHazardLevel(safeInt(params.get("hazardLevel"), 1));
        report.setDescription(params.getOrDefault("description", "").toString());
        report.setLocation(params.getOrDefault("location", "").toString());
        report.setImageUrl(params.getOrDefault("imageUrl", "").toString());
        report.setReportedBy(SecurityContext.getCurrentUserId());

        Long assigneeId = safeLong(params.get("assigneeId"));
        HazardReport saved = hazardService.reportHazard(report, assigneeId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", saved.getId());
        result.put("status", saved.getStatus());
        return Result.ok(result);
    }

    @PostMapping("/queryHazardReportList")
    public Result<PageResult<Map<String, Object>>> queryHazardReportList(@RequestBody(required = false) Map<String, Object> params) {
        Page<HazardReport> pageParam = PageHelper.of(params);
        LambdaQueryWrapper<HazardReport> wrapper = new LambdaQueryWrapper<HazardReport>()
                .orderByDesc(HazardReport::getCreatedAt);

        if (params != null) {
            if (params.containsKey("projectId") && params.get("projectId") != null)
                wrapper.eq(HazardReport::getProjectId, safeLong(params.get("projectId")));
            if (params.containsKey("status") && params.get("status") != null)
                wrapper.eq(HazardReport::getStatus, safeInt(params.get("status"), 0));
            if (params.containsKey("hazardLevel") && params.get("hazardLevel") != null)
                wrapper.eq(HazardReport::getHazardLevel, safeInt(params.get("hazardLevel"), 0));
        }

        Page<HazardReport> page = hazardService.page(pageParam, wrapper);
        List<Map<String, Object>> list = page.getRecords().stream().map(this::reportToMap).collect(Collectors.toList());
        return Result.ok(PageResult.of(list, page.getTotal(), (int) page.getCurrent(), (int) page.getSize()));
    }

    @PostMapping("/getHazardReportDetail")
    public Result<Map<String, Object>> getHazardReportDetail(@RequestBody Map<String, Object> params) {
        Long id = safeLong(params.get("id"));
        Object progress = hazardService.getHazardProgress(id);
        @SuppressWarnings("unchecked")
        Map<String, Object> result = progress instanceof Map ? (Map<String, Object>) progress : new LinkedHashMap<>();
        return Result.ok(result);
    }

    @PostMapping("/getHazardCount")
    public Result<Map<String, Object>> getHazardCount(@RequestBody Map<String, Object> params) {
        Long projectId = safeLong(params.get("projectId"));
        Object stats = hazardService.getHazardStats(projectId);
        @SuppressWarnings("unchecked")
        Map<String, Object> result = stats instanceof Map ? (Map<String, Object>) stats : new LinkedHashMap<>();
        return Result.ok(result);
    }

    @PostMapping("/setHazardReport")
    @OperateLog(module = "隐患管理", action = "update", description = "修改隐患")
    public Result<Void> setHazardReport(@RequestBody Map<String, Object> params) {
        Long id = safeLong(params.get("id"));
        HazardReport report = hazardService.getById(id);
        if (report == null) return Result.fail("隐患不存在");
        if (params.containsKey("status")) report.setStatus(safeInt(params.get("status"), 0));
        if (params.containsKey("description")) report.setDescription(params.get("description").toString());
        if (params.containsKey("hazardLevel")) report.setHazardLevel(safeInt(params.get("hazardLevel"), 0));
        hazardService.updateById(report);
        return Result.ok();
    }

    @PostMapping("/delHazardReport/{id}")
    @OperateLog(module = "隐患管理", action = "delete", description = "删除隐患 #{{id}}", targetId = "{{id}}")
    public Result<Void> delHazardReport(@PathVariable Long id) {
        hazardService.removeById(id);
        return Result.ok();
    }

    @PostMapping("/approveHazard")
    public Result<Void> approveHazard(@RequestBody Map<String, Object> params) {
        Long hazardId = safeLong(params.get("hazardId"));
        String action = params.getOrDefault("action", "pass").toString();
        String comment = params.getOrDefault("comment", "").toString();
        approvalService.processApproval(hazardId, action,
                SecurityContext.getCurrentUserId(),
                SecurityContext.getCurrentUsername(),
                comment);
        return Result.ok();
    }

    @PostMapping("/getApprovalHistory")
    public Result<List<ApprovalRecord>> getApprovalHistory(@RequestBody Map<String, Object> params) {
        Long hazardId = safeLong(params.get("hazardId"));
        return Result.ok(approvalService.getApprovalHistory(hazardId));
    }

    private Map<String, Object> reportToMap(HazardReport r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.getId());
        m.put("projectId", r.getProjectId());
        m.put("reportType", r.getReportType());
        m.put("hazardType", r.getHazardType());
        m.put("hazardLevel", r.getHazardLevel());
        m.put("description", r.getDescription());
        m.put("location", r.getLocation());
        m.put("imageUrl", r.getImageUrl());
        m.put("status", r.getStatus());
        m.put("reportedBy", r.getReportedBy());
        m.put("createdAt", r.getCreatedAt() != null ? r.getCreatedAt().toString() : "");
        return m;
    }

    private Long safeLong(Object v) {
        if (v == null) return null;
        try { return Long.parseLong(v.toString()); } catch (NumberFormatException e) { return null; }
    }
    private int safeInt(Object v, int def) {
        if (v == null) return def;
        try { return Integer.parseInt(v.toString()); } catch (NumberFormatException e) { return def; }
    }
}
