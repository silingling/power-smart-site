package com.powersmart.hazard.controller;

import com.powersmart.common.annotation.OperateLog;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.hazard.entity.InspectionPlan;
import com.powersmart.hazard.service.InspectionPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/build/inspectionPlan")
public class InspectionPlanController {

    private final InspectionPlanService inspectionPlanService;

    @PostMapping("/list")
    public Result<PageResult<InspectionPlan>> list(@RequestBody(required = false) Map<String, Object> params) {
        return Result.ok(inspectionPlanService.list(params));
    }

    @PostMapping("/get/{id}")
    public Result<InspectionPlan> get(@PathVariable Long id) {
        InspectionPlan plan = inspectionPlanService.getById(id);
        if (plan == null) {
            return Result.fail("巡检计划不存在");
        }
        return Result.ok(plan);
    }

    @PostMapping("/add")
    @OperateLog(module = "巡检计划管理", action = "insert", description = "新增巡检计划", recordResult = false)
    public Result<Void> add(@RequestBody InspectionPlan entity) {
        inspectionPlanService.add(entity);
        return Result.ok();
    }

    @PostMapping("/set")
    @OperateLog(module = "巡检计划管理", action = "update", description = "修改巡检计划")
    public Result<Void> set(@RequestBody InspectionPlan entity) {
        inspectionPlanService.update(entity);
        return Result.ok();
    }

    @PostMapping("/del/{id}")
    @OperateLog(module = "巡检计划管理", action = "delete", description = "删除巡检计划 #{{id}}", targetId = "{{id}}")
    public Result<Void> del(@PathVariable Long id) {
        inspectionPlanService.delete(id);
        return Result.ok();
    }

    @PostMapping("/getActivePlans")
    public Result<List<InspectionPlan>> getActivePlans(@RequestBody Map<String, Object> params) {
        Long projectId = params.get("projectId") != null
                ? Long.valueOf(params.get("projectId").toString()) : null;
        return Result.ok(inspectionPlanService.getActivePlans(projectId));
    }
}
