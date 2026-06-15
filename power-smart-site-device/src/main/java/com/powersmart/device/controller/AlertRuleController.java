package com.powersmart.device.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.common.util.PageHelper;
import com.powersmart.device.entity.AlertRule;
import com.powersmart.device.service.AlertRuleEngine;
import com.powersmart.device.mapper.AlertRuleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 告警规则管理 — /build/alertRule/*
 */
@RestController
@RequestMapping("/build/alertRule")
@RequiredArgsConstructor
public class AlertRuleController {

    private final AlertRuleMapper ruleMapper;

    /** 分页查询告警规则列表 */
    @PostMapping("/queryAlertRuleList")
    public Result<PageResult<Map<String, Object>>> queryList(@RequestBody(required = false) Map<String, Object> params) {
        Page<AlertRule> pageParam = PageHelper.of(params);
        Page<AlertRule> page = ruleMapper.selectPage(pageParam, new LambdaQueryWrapper<AlertRule>()
                .orderByAsc(AlertRule::getDeviceType, AlertRule::getSensorType));

        List<Map<String, Object>> list = page.getRecords().stream().map(this::ruleToMap).collect(Collectors.toList());
        return Result.ok(PageResult.of(list, page.getTotal(), (int) page.getCurrent(), (int) page.getSize()));
    }

    /** 新建告警规则 */
    @PostMapping("/addAlertRule")
    public Result<Void> add(@RequestBody Map<String, Object> params) {
        AlertRule rule = buildRuleFromParams(null, params);
        ruleMapper.insert(rule);
        return Result.ok();
    }

    /** 更新告警规则 */
    @PostMapping("/setAlertRule")
    public Result<Void> set(@RequestBody Map<String, Object> params) {
        Long id = safeLong(params.get("id"));
        if (id == null) return Result.fail("id 不能为空");
        AlertRule rule = ruleMapper.selectById(id);
        if (rule == null) return Result.fail("规则不存在");
        buildRuleFromParams(rule, params);
        ruleMapper.updateById(rule);
        return Result.ok();
    }

    /** 删除告警规则 */
    @PostMapping("/delAlertRule/{id}")
    public Result<Void> del(@PathVariable Long id) {
        ruleMapper.deleteById(id);
        return Result.ok();
    }

    /** 获取告警规则详情 */
    @PostMapping("/getAlertRule/{id}")
    public Result<Map<String, Object>> get(@PathVariable Long id) {
        AlertRule rule = ruleMapper.selectById(id);
        if (rule == null) return Result.fail("规则不存在");
        return Result.ok(ruleToMap(rule));
    }

    // ===== 帮助方法 =====

    private Map<String, Object> ruleToMap(AlertRule r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.getId());
        m.put("ruleName", r.getRuleName());
        m.put("deviceType", r.getDeviceType());
        m.put("sensorType", r.getSensorType());
        m.put("operator", r.getOperator());
        m.put("warningThreshold", r.getWarningThreshold());
        m.put("criticalThreshold", r.getCriticalThreshold());
        m.put("durationSeconds", r.getDurationSeconds());
        m.put("enabled", r.getEnabled());
        m.put("remark", r.getRemark());
        return m;
    }

    private AlertRule buildRuleFromParams(AlertRule rule, Map<String, Object> params) {
        if (rule == null) {
            rule = new AlertRule();
            rule.setEnabled(1);
        }
        if (params.containsKey("ruleName")) rule.setRuleName(params.get("ruleName").toString());
        if (params.containsKey("deviceType")) rule.setDeviceType(params.get("deviceType").toString());
        if (params.containsKey("sensorType")) rule.setSensorType(params.get("sensorType").toString());
        if (params.containsKey("operator")) rule.setOperator(params.get("operator").toString());
        if (params.containsKey("warningThreshold"))
            rule.setWarningThreshold(new BigDecimal(params.get("warningThreshold").toString()));
        if (params.containsKey("criticalThreshold"))
            rule.setCriticalThreshold(new BigDecimal(params.get("criticalThreshold").toString()));
        if (params.containsKey("durationSeconds"))
            rule.setDurationSeconds(Integer.parseInt(params.get("durationSeconds").toString()));
        if (params.containsKey("enabled"))
            rule.setEnabled(Integer.parseInt(params.get("enabled").toString()));
        if (params.containsKey("remark")) rule.setRemark(params.get("remark").toString());
        return rule;
    }

    private Long safeLong(Object v) {
        if (v == null) return null;
        try { return Long.parseLong(v.toString()); } catch (NumberFormatException e) { return null; }
    }
}
