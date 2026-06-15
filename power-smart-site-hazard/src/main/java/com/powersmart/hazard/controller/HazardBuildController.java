package com.powersmart.hazard.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.auth.SecurityContext;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.common.util.PageHelper;
import com.powersmart.hazard.entity.*;
import com.powersmart.hazard.mapper.*;
import com.powersmart.hazard.service.ApprovalService;
import com.powersmart.hazard.service.HazardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 隐患管理 — 同业电力前端 build/hazardReport/* + build/hazardWorkOrder/*
 */
@RestController
@RequiredArgsConstructor
public class HazardBuildController {

    private final HazardService hazardService;
    private final ApprovalService approvalService;
    private final HazardReportMapper reportMapper;
    private final HazardWorkOrderMapper workOrderMapper;

    // ==================== 隐患上报 /hazardReport ====================

    /** 上报隐患 */
    @PostMapping("/build/hazardReport/addHazardReport")
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

    /** 分页查询隐患列表 */
    @PostMapping("/build/hazardReport/queryHazardReportList")
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

    /** 隐患详情（含工单 + 审批进展） */
    @PostMapping("/build/hazardReport/getHazardReportDetail")
    public Result<Map<String, Object>> getHazardReportDetail(@RequestBody Map<String, Object> params) {
        Long id = safeLong(params.get("id"));
        Object progress = hazardService.getHazardProgress(id);

        @SuppressWarnings("unchecked")
        Map<String, Object> result = progress instanceof Map ? (Map<String, Object>) progress : new LinkedHashMap<>();
        return Result.ok(result);
    }

    /** 隐患统计 */
    @PostMapping("/build/hazardReport/getHazardCount")
    public Result<Map<String, Object>> getHazardCount(@RequestBody Map<String, Object> params) {
        Long projectId = safeLong(params.get("projectId"));
        Object stats = hazardService.getHazardStats(projectId);
        @SuppressWarnings("unchecked")
        Map<String, Object> result = stats instanceof Map ? (Map<String, Object>) stats : new LinkedHashMap<>();
        return Result.ok(result);
    }

    /** 更新隐患 */
    @PostMapping("/build/hazardReport/setHazardReport")
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

    /** 删除隐患 */
    @PostMapping("/build/hazardReport/delHazardReport/{id}")
    public Result<Void> delHazardReport(@PathVariable Long id) {
        hazardService.removeById(id);
        return Result.ok();
    }

    // ==================== 工单 /hazardWorkOrder ====================

    /** 创建整改工单 */
    @PostMapping("/build/hazardWorkOrder/addWorkOrder")
    public Result<Map<String, Object>> addWorkOrder(@RequestBody Map<String, Object> params) {
        Long hazardId = safeLong(params.get("hazardId"));
        Long assigneeId = safeLong(params.get("assigneeId"));
        Long teamId = safeLong(params.get("teamId"));
        int deadlineHours = safeInt(params.get("deadlineHours"), 48);

        HazardWorkOrder order = hazardService.createWorkOrder(hazardId, assigneeId, teamId, deadlineHours);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", order.getId());
        result.put("status", order.getStatus());
        return Result.ok(result);
    }

    /** 分页查询工单列表 */
    @PostMapping("/build/hazardWorkOrder/queryWorkOrderList")
    public Result<PageResult<Map<String, Object>>> queryWorkOrderList(@RequestBody(required = false) Map<String, Object> params) {
        Page<HazardWorkOrder> pageParam = PageHelper.of(params);
        LambdaQueryWrapper<HazardWorkOrder> wrapper = new LambdaQueryWrapper<HazardWorkOrder>()
                .orderByDesc(HazardWorkOrder::getCreatedAt);

        if (params != null) {
            if (params.containsKey("status") && params.get("status") != null)
                wrapper.eq(HazardWorkOrder::getStatus, safeInt(params.get("status"), 0));
            if (params.containsKey("hazardId") && params.get("hazardId") != null)
                wrapper.eq(HazardWorkOrder::getHazardId, safeLong(params.get("hazardId")));
        }

        Page<HazardWorkOrder> page = workOrderMapper.selectPage(pageParam, wrapper);
        List<Map<String, Object>> list = page.getRecords().stream().map(order -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", order.getId());
            m.put("hazardId", order.getHazardId());
            m.put("assigneeId", order.getAssigneeId());
            m.put("deadline", order.getDeadline() != null ? order.getDeadline().toString() : "");
            m.put("status", order.getStatus());
            m.put("rectificationNote", order.getRectificationNote());
            m.put("verifiedNote", order.getVerifiedNote());
            m.put("escalated", order.getEscalated());
            m.put("createdAt", order.getCreatedAt() != null ? order.getCreatedAt().toString() : "");
            return m;
        }).collect(Collectors.toList());

        return Result.ok(PageResult.of(list, page.getTotal(), (int) page.getCurrent(), (int) page.getSize()));
    }

    /** 提交整改 */
    @PostMapping("/build/hazardWorkOrder/submitRectification")
    public Result<Void> submitRectification(@RequestBody Map<String, Object> params) {
        Long orderId = safeLong(params.get("orderId"));
        String note = params.getOrDefault("note", "").toString();
        String images = params.getOrDefault("images", "").toString();
        hazardService.submitRectification(orderId, note, images);
        return Result.ok();
    }

    /** 验收工单 */
    @PostMapping("/build/hazardWorkOrder/verifyOrder")
    public Result<Void> verifyOrder(@RequestBody Map<String, Object> params) {
        Long orderId = safeLong(params.get("orderId"));
        boolean passed = "3".equals(String.valueOf(params.getOrDefault("status", "3")));
        String note = params.getOrDefault("note", "").toString();
        hazardService.verifyWorkOrder(orderId, SecurityContext.getCurrentUserId(), passed, note);
        return Result.ok();
    }

    // ==================== 审批操作 ====================

    /** 审批隐患 */
    @PostMapping("/build/hazardReport/approveHazard")
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

    /** 获取审批记录 */
    @PostMapping("/build/hazardReport/getApprovalHistory")
    public Result<List<ApprovalRecord>> getApprovalHistory(@RequestBody Map<String, Object> params) {
        Long hazardId = safeLong(params.get("hazardId"));
        return Result.ok(approvalService.getApprovalHistory(hazardId));
    }

    // ==================== 帮助方法 ====================

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
