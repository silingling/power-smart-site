package com.powersmart.hazard.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.annotation.OperateLog;
import com.powersmart.common.auth.SecurityContext;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.common.util.PageHelper;
import com.powersmart.hazard.entity.HazardWorkOrder;
import com.powersmart.hazard.service.HazardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 整改工单管理 — /build/hazardWorkOrder/*
 */
@RestController
@RequestMapping("/build/hazardWorkOrder")
@RequiredArgsConstructor
public class HazardWorkOrderController {

    private final HazardService hazardService;

    @PostMapping("/addWorkOrder")
    @OperateLog(module = "隐患管理", action = "insert", description = "创建整改工单", recordResult = false)
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

    @PostMapping("/queryWorkOrderList")
    public Result<PageResult<Map<String, Object>>> queryWorkOrderList(@RequestBody(required = false) Map<String, Object> params) {
        Page<HazardWorkOrder> page = hazardService.queryWorkOrderPage(PageHelper.of(params), params);
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

    @PostMapping("/submitRectification")
    @OperateLog(module = "隐患管理", action = "update", description = "提交整改")
    public Result<Void> submitRectification(@RequestBody Map<String, Object> params) {
        Long orderId = safeLong(params.get("orderId"));
        String note = params.getOrDefault("note", "").toString();
        String images = params.getOrDefault("images", "").toString();
        hazardService.submitRectification(orderId, note, images);
        return Result.ok();
    }

    @PostMapping("/verifyOrder")
    @OperateLog(module = "隐患管理", action = "verify", description = "验收工单")
    public Result<Void> verifyOrder(@RequestBody Map<String, Object> params) {
        Long orderId = safeLong(params.get("orderId"));
        boolean passed = "3".equals(String.valueOf(params.getOrDefault("status", "3")));
        String note = params.getOrDefault("note", "").toString();
        hazardService.verifyWorkOrder(orderId, SecurityContext.getCurrentUserId(), passed, note);
        return Result.ok();
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
