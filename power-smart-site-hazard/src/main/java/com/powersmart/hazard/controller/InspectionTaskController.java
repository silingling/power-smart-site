package com.powersmart.hazard.controller;

import com.powersmart.common.annotation.OperateLog;
import com.powersmart.common.auth.SecurityContext;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.hazard.entity.InspectionTask;
import com.powersmart.hazard.service.InspectionTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/build/inspectionTask")
public class InspectionTaskController {

    private final InspectionTaskService inspectionTaskService;

    @PostMapping("/list")
    public Result<PageResult<InspectionTask>> list(@RequestBody(required = false) Map<String, Object> params) {
        return Result.ok(inspectionTaskService.list(params));
    }

    @PostMapping("/get/{id}")
    public Result<InspectionTask> get(@PathVariable Long id) {
        InspectionTask task = inspectionTaskService.getById(id);
        if (task == null) {
            return Result.fail("巡检任务不存在");
        }
        return Result.ok(task);
    }

    @PostMapping("/add")
    @OperateLog(module = "巡检任务管理", action = "insert", description = "新增巡检任务", recordResult = false)
    public Result<Void> add(@RequestBody InspectionTask entity) {
        inspectionTaskService.add(entity);
        return Result.ok();
    }

    @PostMapping("/set")
    @OperateLog(module = "巡检任务管理", action = "update", description = "修改巡检任务")
    public Result<Void> set(@RequestBody InspectionTask entity) {
        inspectionTaskService.update(entity);
        return Result.ok();
    }

    @PostMapping("/del/{id}")
    @OperateLog(module = "巡检任务管理", action = "delete", description = "删除巡检任务 #{{id}}", targetId = "{{id}}")
    public Result<Void> del(@PathVariable Long id) {
        inspectionTaskService.delete(id);
        return Result.ok();
    }

    @PostMapping("/generateTasks")
    @OperateLog(module = "巡检任务管理", action = "generate", description = "生成巡检任务", recordResult = false)
    public Result<Map<String, Object>> generateTasks(@RequestBody Map<String, Object> params) {
        Long planId = params.get("planId") != null
                ? Long.valueOf(params.get("planId").toString()) : null;
        LocalDate date = params.get("date") != null
                ? LocalDate.parse(params.get("date").toString()) : LocalDate.now();

        if (planId == null) {
            return Result.fail("计划ID不能为空");
        }

        int count = inspectionTaskService.generateTasks(planId, date);
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        result.put("planId", planId);
        result.put("date", date.toString());
        return Result.ok(result);
    }

    @PostMapping("/updateStatus")
    @OperateLog(module = "巡检任务管理", action = "update", description = "更新任务状态")
    public Result<Void> updateStatus(@RequestBody Map<String, Object> params) {
        Long id = params.get("id") != null
                ? Long.valueOf(params.get("id").toString()) : null;
        String status = params.getOrDefault("status", "").toString();

        if (id == null) {
            return Result.fail("任务ID不能为空");
        }
        inspectionTaskService.updateStatus(id, status);
        return Result.ok();
    }

    @PostMapping("/getMyPendingTasks")
    public Result<List<InspectionTask>> getMyPendingTasks() {
        Long userId = SecurityContext.getCurrentUserId();
        return Result.ok(inspectionTaskService.getMyPendingTasks(userId));
    }

    @PostMapping("/getStats")
    public Result<Map<String, Object>> getStats(@RequestBody Map<String, Object> params) {
        Long projectId = params.get("projectId") != null
                ? Long.valueOf(params.get("projectId").toString()) : null;
        return Result.ok(inspectionTaskService.getTaskStats(projectId));
    }
}
