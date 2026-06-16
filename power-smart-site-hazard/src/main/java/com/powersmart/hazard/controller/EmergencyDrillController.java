package com.powersmart.hazard.controller;

import com.powersmart.common.annotation.OperateLog;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.hazard.entity.EmergencyDrill;
import com.powersmart.hazard.service.EmergencyDrillService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/build/emergencyDrill")
@RequiredArgsConstructor
public class EmergencyDrillController {

    private final EmergencyDrillService emergencyDrillService;

    @PostMapping("/list")
    public Result<PageResult<EmergencyDrill>> list(@RequestBody Map<String, Object> params) {
        return Result.ok(emergencyDrillService.list(params));
    }

    @PostMapping("/get/{id}")
    public Result<EmergencyDrill> get(@PathVariable Long id) {
        return Result.ok(emergencyDrillService.getById(id));
    }

    @PostMapping("/add")
    @OperateLog(module = "应急管理", action = "insert", description = "新增应急演练")
    public Result<Void> add(@RequestBody EmergencyDrill entity) {
        emergencyDrillService.add(entity);
        return Result.ok();
    }

    @PostMapping("/set")
    @OperateLog(module = "应急管理", action = "update", description = "修改应急演练")
    public Result<Void> set(@RequestBody EmergencyDrill entity) {
        emergencyDrillService.update(entity);
        return Result.ok();
    }

    @PostMapping("/del/{id}")
    @OperateLog(module = "应急管理", action = "delete", description = "删除应急演练 #{{id}}", targetId = "{{id}}")
    public Result<Void> del(@PathVariable Long id) {
        emergencyDrillService.delete(id);
        return Result.ok();
    }

    @PostMapping("/getStats")
    public Result<Map<String, Object>> getStats(@RequestBody Map<String, Object> params) {
        Long projectId = params.get("projectId") != null
                ? Long.valueOf(params.get("projectId").toString()) : null;
        Integer year = params.get("year") != null
                ? Integer.valueOf(params.get("year").toString()) : null;
        return Result.ok(emergencyDrillService.getDrillStats(projectId, year));
    }
}
