package com.powersmart.device.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.device.entity.MonitorPointAlert;
import com.powersmart.device.mapper.MonitorPointAlertMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 监测点告警 — 同业电力前端 build/monitorPointAlert/*
 */
@RestController
@RequestMapping("/build/monitorPointAlert")
@RequiredArgsConstructor
public class MonitorPointAlertController {

    private final MonitorPointAlertMapper mapper;

    @PostMapping("/selectNumber")
    public Result<Long> selectNumber(@RequestBody Map<String, Object> params) {
        LambdaQueryWrapper<MonitorPointAlert> wrapper = new LambdaQueryWrapper<MonitorPointAlert>()
                .eq(MonitorPointAlert::getStatus, 0);
        if (params != null && params.containsKey("projectId") && params.get("projectId") != null)
            wrapper.eq(MonitorPointAlert::getProjectId, Long.valueOf(params.get("projectId").toString()));
        return Result.ok(mapper.selectCount(wrapper));
    }

    @PostMapping("/selectState")
    public Result<List<Map<String, Object>>> selectState(@RequestBody(required = false) Map<String, Object> params) {
        List<Map<String, Object>> result = new ArrayList<>();
        // 按 status 分组统计
        Map<Integer, Long> countMap = new LinkedHashMap<>();
        for (MonitorPointAlert alert : mapper.selectList(new LambdaQueryWrapper<>())) {
            int status = alert.getStatus() != null ? alert.getStatus() : 0;
            countMap.merge(status, 1L, Long::sum);
        }
        for (Map.Entry<Integer, Long> entry : countMap.entrySet()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("status", entry.getKey());
            item.put("count", entry.getValue());
            result.add(item);
        }
        return Result.ok(result);
    }

    @PostMapping("/selectUntreatedDataList")
    public Result<PageResult<MonitorPointAlert>> selectUntreatedDataList(@RequestBody(required = false) Map<String, Object> params) {
        Page<MonitorPointAlert> page = extractPage(params);
        LambdaQueryWrapper<MonitorPointAlert> wrapper = new LambdaQueryWrapper<MonitorPointAlert>()
                .eq(MonitorPointAlert::getStatus, 0);
        if (params != null && params.containsKey("projectId") && params.get("projectId") != null)
            wrapper.eq(MonitorPointAlert::getProjectId, Long.valueOf(params.get("projectId").toString()));
        wrapper.orderByDesc(MonitorPointAlert::getCreateTime);
        return Result.ok(PageResult.from(mapper.selectPage(page, wrapper)));
    }

    @SuppressWarnings("unchecked")
    private Page<MonitorPointAlert> extractPage(Map<String, Object> params) {
        int p = 1, s = 20;
        if (params != null) {
            if (params.get("page") != null) try { p = Integer.parseInt(params.get("page").toString()); } catch (NumberFormatException ignored) {}
            if (params.get("limit") != null) try { s = Integer.parseInt(params.get("limit").toString()); } catch (NumberFormatException ignored) {}
            if (params.get("pageSize") != null) try { s = Integer.parseInt(params.get("pageSize").toString()); } catch (NumberFormatException ignored) {}
        }
        return new Page<>(p, s);
    }
}
