package com.powersmart.device.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.device.entity.SupplyPoint;
import com.powersmart.device.mapper.SupplyPointMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 水电供应点管理 — 同业电力前端 adminSupplyPoint/*
 */
@RestController
@RequestMapping("/adminSupplyPoint")
@RequiredArgsConstructor
public class SupplyPointController {

    private final SupplyPointMapper mapper;

    @PostMapping("/list")
    public Result<PageResult<SupplyPoint>> list(@RequestBody(required = false) Map<String, Object> params) {
        Page<SupplyPoint> page = extractPage(params);
        LambdaQueryWrapper<SupplyPoint> wrapper = new LambdaQueryWrapper<>();
        if (params != null) {
            if (params.containsKey("projectId") && params.get("projectId") != null)
                wrapper.eq(SupplyPoint::getProjectId, Long.valueOf(params.get("projectId").toString()));
            if (params.containsKey("pointType") && params.get("pointType") != null && StrUtil.isNotBlank(params.get("pointType").toString()))
                wrapper.eq(SupplyPoint::getPointType, params.get("pointType").toString());
        }
        wrapper.orderByDesc(SupplyPoint::getCreateTime);
        return Result.ok(PageResult.from(mapper.selectPage(page, wrapper)));
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody SupplyPoint entity) {
        mapper.insert(entity);
        return Result.ok();
    }

    @PostMapping("/edit")
    public Result<Void> edit(@RequestBody SupplyPoint entity) {
        mapper.updateById(entity);
        return Result.ok();
    }

    @PostMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        mapper.deleteById(id);
        return Result.ok();
    }

    @PostMapping("/waterRealTimeData/{pointId}")
    public Result<Map<String, Object>> waterRealTimeData(@PathVariable Long pointId) {
        SupplyPoint point = mapper.selectById(pointId);
        Map<String, Object> data = new LinkedHashMap<>();
        if (point != null) {
            data.put("pointId", point.getId());
            data.put("pointName", point.getPointName());
            data.put("currentReading", point.getCurrentReading());
        } else {
            data.put("pointId", pointId);
            data.put("currentReading", 0);
        }
        data.put("updateTime", new Date());
        return Result.ok(data);
    }

    @PostMapping("/getHistoryCurveData")
    public Result<List<Map<String, Object>>> getHistoryCurveData(@RequestBody(required = false) Map<String, Object> params) {
        // TODO: 实现历史曲线数据查询
        return Result.ok(new ArrayList<>());
    }

    @PostMapping("/getHistoryReportData")
    public Result<List<Map<String, Object>>> getHistoryReportData(@RequestBody(required = false) Map<String, Object> params) {
        // TODO: 实现历史报表数据查询
        return Result.ok(new ArrayList<>());
    }

    @SuppressWarnings("unchecked")
    private Page<SupplyPoint> extractPage(Map<String, Object> params) {
        int p = 1, s = 20;
        if (params != null) {
            if (params.get("page") != null) try { p = Integer.parseInt(params.get("page").toString()); } catch (NumberFormatException ignored) {}
            if (params.get("limit") != null) try { s = Integer.parseInt(params.get("limit").toString()); } catch (NumberFormatException ignored) {}
            if (params.get("pageSize") != null) try { s = Integer.parseInt(params.get("pageSize").toString()); } catch (NumberFormatException ignored) {}
        }
        return new Page<>(p, s);
    }
}
