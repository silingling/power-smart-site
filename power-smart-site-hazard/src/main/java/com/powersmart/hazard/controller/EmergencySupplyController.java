package com.powersmart.hazard.controller;

import com.powersmart.common.annotation.OperateLog;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.hazard.entity.EmergencySupply;
import com.powersmart.hazard.service.EmergencySupplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/build/emergencySupply")
@RequiredArgsConstructor
public class EmergencySupplyController {

    private final EmergencySupplyService emergencySupplyService;

    @PostMapping("/list")
    public Result<PageResult<EmergencySupply>> list(@RequestBody Map<String, Object> params) {
        return Result.ok(emergencySupplyService.list(params));
    }

    @PostMapping("/get/{id}")
    public Result<EmergencySupply> get(@PathVariable Long id) {
        return Result.ok(emergencySupplyService.getById(id));
    }

    @PostMapping("/add")
    @OperateLog(module = "应急管理", action = "insert", description = "新增应急物资")
    public Result<Void> add(@RequestBody EmergencySupply entity) {
        emergencySupplyService.add(entity);
        return Result.ok();
    }

    @PostMapping("/set")
    @OperateLog(module = "应急管理", action = "update", description = "修改应急物资")
    public Result<Void> set(@RequestBody EmergencySupply entity) {
        emergencySupplyService.update(entity);
        return Result.ok();
    }

    @PostMapping("/del/{id}")
    @OperateLog(module = "应急管理", action = "delete", description = "删除应急物资 #{{id}}", targetId = "{{id}}")
    public Result<Void> del(@PathVariable Long id) {
        emergencySupplyService.delete(id);
        return Result.ok();
    }

    @PostMapping("/getLowStock")
    public Result<List<EmergencySupply>> getLowStock(@RequestBody Map<String, Object> params) {
        Long projectId = params.get("projectId") != null
                ? Long.valueOf(params.get("projectId").toString()) : null;
        return Result.ok(emergencySupplyService.getLowStockList(projectId));
    }

    @PostMapping("/getInventoryStats")
    public Result<Map<String, Object>> getInventoryStats(@RequestBody Map<String, Object> params) {
        Long projectId = params.get("projectId") != null
                ? Long.valueOf(params.get("projectId").toString()) : null;
        return Result.ok(emergencySupplyService.getInventoryStats(projectId));
    }
}
