package com.powersmart.device.controller;

import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.device.entity.MonitorPointAlert;
import com.powersmart.device.service.MonitorPointAlertService;
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

    private final MonitorPointAlertService monitorPointAlertService;

    @PostMapping("/selectNumber")
    public Result<Long> selectNumber(@RequestBody Map<String, Object> params) {
        return Result.ok(monitorPointAlertService.selectNumber(params));
    }

    @PostMapping("/selectState")
    public Result<List<Map<String, Object>>> selectState(@RequestBody(required = false) Map<String, Object> params) {
        return Result.ok(monitorPointAlertService.selectState(params));
    }

    @PostMapping("/selectUntreatedDataList")
    public Result<PageResult<MonitorPointAlert>> selectUntreatedDataList(@RequestBody(required = false) Map<String, Object> params) {
        return Result.ok(monitorPointAlertService.selectUntreatedDataList(params));
    }
}
