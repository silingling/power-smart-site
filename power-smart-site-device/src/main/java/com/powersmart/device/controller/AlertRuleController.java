package com.powersmart.device.controller;

import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.device.service.AlertRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 告警规则管理 — /build/alertRule/*
 */
@RestController
@RequestMapping("/build/alertRule")
@RequiredArgsConstructor
public class AlertRuleController {

    private final AlertRuleService alertRuleService;

    /** 分页查询告警规则列表 */
    @PostMapping("/queryAlertRuleList")
    public Result<PageResult<Map<String, Object>>> queryList(@RequestBody(required = false) Map<String, Object> params) {
        return Result.ok(alertRuleService.queryList(params));
    }

    /** 新建告警规则 */
    @PostMapping("/addAlertRule")
    public Result<Void> add(@RequestBody Map<String, Object> params) {
        alertRuleService.add(params);
        return Result.ok();
    }

    /** 更新告警规则 */
    @PostMapping("/setAlertRule")
    public Result<Void> set(@RequestBody Map<String, Object> params) {
        Long id = safeLong(params.get("id"));
        if (id == null) return Result.fail("id 不能为空");
        alertRuleService.set(id, params);
        return Result.ok();
    }

    /** 删除告警规则 */
    @PostMapping("/delAlertRule/{id}")
    public Result<Void> del(@PathVariable Long id) {
        alertRuleService.delete(id);
        return Result.ok();
    }

    /** 获取告警规则详情 */
    @PostMapping("/getAlertRule/{id}")
    public Result<Map<String, Object>> get(@PathVariable Long id) {
        try {
            return Result.ok(alertRuleService.get(id));
        } catch (IllegalArgumentException e) {
            return Result.fail(e.getMessage());
        }
    }

    private Long safeLong(Object v) {
        if (v == null) return null;
        try { return Long.parseLong(v.toString()); } catch (NumberFormatException e) { return null; }
    }
}
