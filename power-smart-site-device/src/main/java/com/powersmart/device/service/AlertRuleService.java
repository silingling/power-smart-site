package com.powersmart.device.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.util.PageHelper;
import com.powersmart.device.entity.AlertRule;
import com.powersmart.device.mapper.AlertRuleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 告警规则 CRUD 服务
 */
@RequiredArgsConstructor
@Service
public class AlertRuleService {

    private final AlertRuleMapper ruleMapper;

    public PageResult<Map<String, Object>> queryList(Map<String, Object> params) {
        Page<AlertRule> pageParam = PageHelper.of(params);
        Page<AlertRule> page = ruleMapper.selectPage(pageParam, new LambdaQueryWrapper<AlertRule>()
                .orderByAsc(AlertRule::getDeviceType, AlertRule::getSensorType));

        List<Map<String, Object>> list = page.getRecords().stream().map(this::ruleToMap).collect(Collectors.toList());
        return PageResult.of(list, page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    public void add(Map<String, Object> params) {
        AlertRule rule = buildRuleFromParams(null, params);
        ruleMapper.insert(rule);
    }

    public void set(Long id, Map<String, Object> params) {
        AlertRule rule = ruleMapper.selectById(id);
        if (rule == null) throw new IllegalArgumentException("规则不存在");
        buildRuleFromParams(rule, params);
        ruleMapper.updateById(rule);
    }

    public void delete(Long id) {
        ruleMapper.deleteById(id);
    }

    public Map<String, Object> get(Long id) {
        AlertRule rule = ruleMapper.selectById(id);
        if (rule == null) throw new IllegalArgumentException("规则不存在");
        return ruleToMap(rule);
    }

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
}
