package com.powersmart.device.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.device.entity.FenceAlertEvent;
import com.powersmart.device.service.SafetyFenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 围栏告警事件管理 — /build/fenceAlertEvent/*
 *
 * <p>告警列表、详情查询、处理、忽略</p>
 */
@RestController
@RequiredArgsConstructor
public class FenceAlertEventController {

    private final SafetyFenceService fenceService;

    /** 分页查询告警事件 */
    @PostMapping("/build/fenceAlertEvent/queryFenceAlertEventList")
    public Result<PageResult<Map<String, Object>>> queryList(@RequestBody(required = false) Map<String, Object> params) {
        Page<FenceAlertEvent> page = fenceService.queryEventPage(params);
        List<Map<String, Object>> list = page.getRecords().stream()
                .map(this::toMap).collect(Collectors.toList());
        return Result.ok(PageResult.of(list, page.getTotal(), (int) page.getCurrent(), (int) page.getSize()));
    }

    /** 获取告警详情 */
    @PostMapping("/build/fenceAlertEvent/getFenceAlertEvent/{id}")
    public Result<Map<String, Object>> get(@PathVariable Long id) {
        FenceAlertEvent event = fenceService.getEventById(id);
        if (event == null) return Result.fail("告警事件不存在");
        return Result.ok(toMap(event));
    }

    /** 处理告警（标记为已处理） */
    @PostMapping("/build/fenceAlertEvent/processFenceAlertEvent")
    public Result<Void> process(@RequestBody Map<String, Object> params) {
        Long id = safeLong(params.get("id"));
        String processedBy = params.containsKey("processedBy") ? params.get("processedBy").toString() : null;
        if (id == null) return Result.fail("id 不能为空");
        fenceService.processEvent(id, processedBy);
        return Result.ok();
    }

    /** 忽略告警 */
    @PostMapping("/build/fenceAlertEvent/ignoreFenceAlertEvent")
    public Result<Void> ignore(@RequestBody Map<String, Object> params) {
        Long id = safeLong(params.get("id"));
        if (id == null) return Result.fail("id 不能为空");
        fenceService.ignoreEvent(id);
        return Result.ok();
    }

    /** 获取未处理的告警数量 */
    @PostMapping("/build/fenceAlertEvent/getPendingFenceAlertCount")
    public Result<Map<String, Long>> getPendingCount(@RequestBody(required = false) Map<String, Object> params) {
        Map<String, Object> query = new LinkedHashMap<>();
        query.put("status", "pending");
        if (params != null && params.containsKey("projectId"))
            query.put("projectId", params.get("projectId"));
        Page<FenceAlertEvent> page = fenceService.queryEventPage(query);
        Map<String, Long> result = new LinkedHashMap<>();
        result.put("pendingCount", page.getTotal());
        return Result.ok(result);
    }

    // ==================== 帮助方法 ====================

    private Map<String, Object> toMap(FenceAlertEvent e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", e.getId());
        m.put("fenceId", e.getFenceId());
        m.put("projectId", e.getProjectId());
        m.put("eventType", e.getEventType());
        m.put("targetType", e.getTargetType());
        m.put("targetId", e.getTargetId());
        m.put("targetName", e.getTargetName());
        m.put("eventLat", e.getEventLat());
        m.put("eventLng", e.getEventLng());
        m.put("description", e.getDescription());
        m.put("status", e.getStatus());
        m.put("processedBy", e.getProcessedBy());
        m.put("processedAt", e.getProcessedAt() != null ? e.getProcessedAt().toString() : "");
        m.put("remark", e.getRemark());
        m.put("createdAt", e.getCreatedAt() != null ? e.getCreatedAt().toString() : "");
        return m;
    }

    private Long safeLong(Object v) {
        if (v == null) return null;
        try { return Long.parseLong(v.toString()); } catch (NumberFormatException e) { return null; }
    }
}
