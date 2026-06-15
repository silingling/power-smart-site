package com.powersmart.device.controller;

import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.device.entity.SupplyPoint;
import com.powersmart.device.service.SupplyPointService;
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

    private final SupplyPointService supplyPointService;

    @PostMapping("/list")
    public Result<PageResult<SupplyPoint>> list(@RequestBody(required = false) Map<String, Object> params) {
        return Result.ok(supplyPointService.list(params));
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody SupplyPoint entity) {
        supplyPointService.add(entity);
        return Result.ok();
    }

    @PostMapping("/edit")
    public Result<Void> edit(@RequestBody SupplyPoint entity) {
        supplyPointService.edit(entity);
        return Result.ok();
    }

    @PostMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        supplyPointService.delete(id);
        return Result.ok();
    }

    @PostMapping("/waterRealTimeData/{pointId}")
    public Result<Map<String, Object>> waterRealTimeData(@PathVariable Long pointId) {
        return Result.ok(supplyPointService.waterRealTimeData(pointId));
    }

    @PostMapping("/getHistoryCurveData")
    public Result<List<Map<String, Object>>> getHistoryCurveData(@RequestBody(required = false) Map<String, Object> params) {
        return Result.ok(supplyPointService.getHistoryCurveData(params));
    }

    @PostMapping("/getHistoryReportData")
    public Result<List<Map<String, Object>>> getHistoryReportData(@RequestBody(required = false) Map<String, Object> params) {
        return Result.ok(supplyPointService.getHistoryReportData(params));
    }
}
