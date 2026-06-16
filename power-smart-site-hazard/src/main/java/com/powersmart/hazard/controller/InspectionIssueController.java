package com.powersmart.hazard.controller;

import com.powersmart.common.annotation.OperateLog;
import com.powersmart.common.auth.SecurityContext;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.hazard.entity.InspectionIssue;
import com.powersmart.hazard.service.InspectionIssueService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/build/inspectionIssue")
public class InspectionIssueController {

    private final InspectionIssueService inspectionIssueService;

    @PostMapping("/list")
    public Result<PageResult<InspectionIssue>> list(@RequestBody(required = false) Map<String, Object> params) {
        return Result.ok(inspectionIssueService.list(params));
    }

    @PostMapping("/get/{id}")
    public Result<InspectionIssue> get(@PathVariable Long id) {
        InspectionIssue issue = inspectionIssueService.getById(id);
        if (issue == null) {
            return Result.fail("巡检问题不存在");
        }
        return Result.ok(issue);
    }

    @PostMapping("/add")
    @OperateLog(module = "巡检问题管理", action = "insert", description = "新增巡检问题", recordResult = false)
    public Result<Void> add(@RequestBody InspectionIssue entity) {
        inspectionIssueService.add(entity);
        return Result.ok();
    }

    @PostMapping("/set")
    @OperateLog(module = "巡检问题管理", action = "update", description = "修改巡检问题")
    public Result<Void> set(@RequestBody InspectionIssue entity) {
        inspectionIssueService.update(entity);
        return Result.ok();
    }

    @PostMapping("/del/{id}")
    @OperateLog(module = "巡检问题管理", action = "delete", description = "删除巡检问题 #{{id}}", targetId = "{{id}}")
    public Result<Void> del(@PathVariable Long id) {
        inspectionIssueService.delete(id);
        return Result.ok();
    }

    @PostMapping("/updateStatus")
    @OperateLog(module = "巡检问题管理", action = "update", description = "更新问题处理状态")
    public Result<Void> updateStatus(@RequestBody Map<String, Object> params) {
        Long id = params.get("id") != null
                ? Long.valueOf(params.get("id").toString()) : null;
        String status = params.getOrDefault("status", "").toString();
        Long handlerId = params.get("handlerId") != null
                ? Long.valueOf(params.get("handlerId").toString()) : SecurityContext.getCurrentUserId();
        String handlerName = params.getOrDefault("handlerName", SecurityContext.getCurrentUsername()).toString();
        String handleMeasure = params.getOrDefault("handleMeasure", "").toString();

        if (id == null) {
            return Result.fail("问题ID不能为空");
        }
        inspectionIssueService.updateStatus(id, status, handlerId, handlerName, handleMeasure);
        return Result.ok();
    }
}
