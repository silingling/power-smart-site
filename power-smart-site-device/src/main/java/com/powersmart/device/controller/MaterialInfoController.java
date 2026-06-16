package com.powersmart.device.controller;

import com.powersmart.common.annotation.OperateLog;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.device.entity.MaterialInfo;
import com.powersmart.device.service.MaterialInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/build/materialInfo")
public class MaterialInfoController {

    private final MaterialInfoService materialInfoService;

    @PostMapping("/list")
    public Result<PageResult<MaterialInfo>> list(@RequestBody(required = false) Map<String, Object> params) {
        return Result.ok(materialInfoService.list(params));
    }

    @PostMapping("/get/{id}")
    public Result<MaterialInfo> getById(@PathVariable Long id) {
        return Result.ok(materialInfoService.getById(id));
    }

    @PostMapping("/add")
    @OperateLog(action = "add", description = "新增物料")
    public Result<Void> add(@RequestBody MaterialInfo entity) {
        return materialInfoService.add(entity);
    }

    @PostMapping("/set")
    @OperateLog(action = "update", description = "更新物料")
    public Result<Void> set(@RequestBody MaterialInfo entity) {
        return materialInfoService.update(entity);
    }

    @PostMapping("/del/{id}")
    @OperateLog(action = "delete", description = "删除物料")
    public Result<Void> del(@PathVariable Long id) {
        return materialInfoService.delete(id);
    }

    @PostMapping("/getLowStock")
    public Result<List<MaterialInfo>> getLowStock(@RequestBody Map<String, Object> params) {
        Long projectId = params != null && params.get("projectId") != null
                ? Long.valueOf(params.get("projectId").toString()) : null;
        return Result.ok(materialInfoService.getLowStockList(projectId));
    }

    @PostMapping("/getInventoryStats")
    public Result<Map<String, Object>> getInventoryStats(@RequestBody Map<String, Object> params) {
        Long projectId = params != null && params.get("projectId") != null
                ? Long.valueOf(params.get("projectId").toString()) : null;
        return Result.ok(materialInfoService.getInventoryStats(projectId));
    }
}
