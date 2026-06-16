package com.powersmart.hazard.controller;

import com.powersmart.common.annotation.OperateLog;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.hazard.entity.EmergencyIncident;
import com.powersmart.hazard.service.EmergencyIncidentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/build/emergencyIncident")
@RequiredArgsConstructor
public class EmergencyIncidentController {

    private final EmergencyIncidentService emergencyIncidentService;

    @PostMapping("/list")
    public Result<PageResult<EmergencyIncident>> list(@RequestBody Map<String, Object> params) {
        return Result.ok(emergencyIncidentService.list(params));
    }

    @PostMapping("/get/{id}")
    public Result<EmergencyIncident> get(@PathVariable Long id) {
        return Result.ok(emergencyIncidentService.getById(id));
    }

    @PostMapping("/add")
    @OperateLog(module = "应急管理", action = "insert", description = "新增应急事件")
    public Result<Void> add(@RequestBody EmergencyIncident entity) {
        emergencyIncidentService.add(entity);
        return Result.ok();
    }

    @PostMapping("/set")
    @OperateLog(module = "应急管理", action = "update", description = "修改应急事件")
    public Result<Void> set(@RequestBody EmergencyIncident entity) {
        emergencyIncidentService.update(entity);
        return Result.ok();
    }

    @PostMapping("/del/{id}")
    @OperateLog(module = "应急管理", action = "delete", description = "删除应急事件 #{{id}}", targetId = "{{id}}")
    public Result<Void> del(@PathVariable Long id) {
        emergencyIncidentService.delete(id);
        return Result.ok();
    }

    @PostMapping("/updateStatus")
    @OperateLog(module = "应急管理", action = "update", description = "更新应急事件状态")
    public Result<Void> updateStatus(@RequestBody Map<String, Object> params) {
        Long id = params.get("id") != null ? Long.valueOf(params.get("id").toString()) : null;
        String status = params.get("status") != null ? params.get("status").toString() : null;
        emergencyIncidentService.updateStatus(id, status);
        return Result.ok();
    }

    @PostMapping("/getStats")
    public Result<Map<String, Object>> getStats(@RequestBody Map<String, Object> params) {
        Long projectId = params.get("projectId") != null
                ? Long.valueOf(params.get("projectId").toString()) : null;
        return Result.ok(emergencyIncidentService.getIncidentStats(projectId));
    }
}
