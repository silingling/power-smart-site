package com.powersmart.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.annotation.OperateLog;
import com.powersmart.common.entity.OperateLog;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.common.service.OperateLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 操作审计日志查询 — /build/operateLog/*
 *
 * <p>提供多维度日志检索功能。</p>
 */
@RestController
@RequestMapping("/build/operateLog")
@RequiredArgsConstructor
public class OperateLogController {

    private final OperateLogService operateLogService;

    /**
     * 分页查询操作日志（支持多条件筛选）
     */
    @PostMapping("/queryPage")
    @OperateLog(module = "审计日志", action = "query", description = "查询操作日志列表",
                recordResult = false)
    public Result<PageResult<OperateLog>> queryPage(@RequestBody(required = false) Map<String, Object> params) {
        int page = params != null && params.containsKey("page") ? Integer.parseInt(params.get("page").toString()) : 1;
        int size = params != null && params.containsKey("size") ? Integer.parseInt(params.get("size").toString()) : 20;
        String module = params != null ? (String) params.get("module") : null;
        String action = params != null ? (String) params.get("action") : null;
        Long operatorId = params != null && params.containsKey("operatorId")
                ? Long.parseLong(params.get("operatorId").toString()) : null;
        String startTimeStr = params != null ? (String) params.get("startTime") : null;
        String endTimeStr = params != null ? (String) params.get("endTime") : null;
        String keyword = params != null ? (String) params.get("keyword") : null;

        LocalDateTime startTime = startTimeStr != null ? LocalDateTime.parse(startTimeStr) : null;
        LocalDateTime endTime = endTimeStr != null ? LocalDateTime.parse(endTimeStr) : null;

        Page<OperateLog> pageResult = operateLogService.queryPage(page, size, module, action,
                operatorId, startTime, endTime, keyword);
        return Result.ok(PageResult.of(pageResult.getRecords(), pageResult.getTotal(), page, size));
    }

    /**
     * 获取最近的操作日志（供看板使用）
     */
    @GetMapping("/recent")
    public Result<List<OperateLog>> getRecent(@RequestParam(defaultValue = "10") int limit) {
        return Result.ok(operateLogService.getRecent(limit));
    }

    /**
     * 按模块统计操作次数
     */
    @GetMapping("/countByModule")
    public Result<List<Map<String, Object>>> countByModule(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return Result.ok(operateLogService.countByModule(startTime, endTime));
    }
}
