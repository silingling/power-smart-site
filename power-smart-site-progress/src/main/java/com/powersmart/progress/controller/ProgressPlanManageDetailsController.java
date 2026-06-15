package com.powersmart.progress.controller;

import com.powersmart.common.entity.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 进度计划管理详情 — 同业电力前端 build/progressPlanManageDetails/*
 */
@RestController
@RequestMapping("/build/progressPlanManageDetails")
@RequiredArgsConstructor
public class ProgressPlanManageDetailsController {

    @PostMapping("/selectPlanLastTimeSHOUYE")
    public Result<Map<String, Object>> selectPlanLastTimeSHOUYE(@RequestBody(required = false) Map<String, Object> params) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("planName", "总体进度计划");
        data.put("startDate", "2025-01-01");
        data.put("endDate", "2025-12-31");
        data.put("completionRate", 85.5);
        data.put("totalTasks", 120);
        data.put("completedTasks", 103);
        data.put("delayedTasks", 5);
        return Result.ok(data);
    }
}
