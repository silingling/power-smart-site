package com.powersmart.hazard.controller;

import com.powersmart.common.annotation.OperateLog;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.hazard.entity.EmergencyPlan;
import com.powersmart.hazard.service.EmergencyPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/build/emergencyPlan")
@RequiredArgsConstructor
public class EmergencyPlanController {

    private final EmergencyPlanService emergencyPlanService;

    @PostMapping("/list")
    public Result<PageResult<EmergencyPlan>> list(@RequestBody Map<String, Object> params) {
        return Result.ok(emergencyPlanService.list(params));
    }

    @PostMapping("/get/{id}")
    public Result<EmergencyPlan> get(@PathVariable Long id) {
        return Result.ok(emergencyPlanService.getById(id));
    }

    @PostMapping("/add")
    @OperateLog(module = "应急管理", action = "insert", description = "新增应急预案")
    public Result<Void> add(@RequestBody EmergencyPlan entity) {
        emergencyPlanService.add(entity);
        return Result.ok();
    }

    @PostMapping("/set")
    @OperateLog(module = "应急管理", action = "update", description = "修改应急预案")
    public Result<Void> set(@RequestBody EmergencyPlan entity) {
        emergencyPlanService.update(entity);
        return Result.ok();
    }

    @PostMapping("/del/{id}")
    @OperateLog(module = "应急管理", action = "delete", description = "删除应急预案 #{{id}}", targetId = "{{id}}")
    public Result<Void> del(@PathVariable Long id) {
        emergencyPlanService.delete(id);
        return Result.ok();
    }

    @PostMapping("/getActivePlans")
    public Result<List<EmergencyPlan>> getActivePlans(@RequestBody Map<String, Object> params) {
        Long projectId = params.get("projectId") != null
                ? Long.valueOf(params.get("projectId").toString()) : null;
        return Result.ok(emergencyPlanService.getActivePlans(projectId));
    }
}
