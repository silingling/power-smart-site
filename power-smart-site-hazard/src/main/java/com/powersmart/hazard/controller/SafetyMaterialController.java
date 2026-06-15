package com.powersmart.hazard.controller;

import cn.hutool.core.util.StrUtil;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.hazard.entity.SafetyMaterial;
import com.powersmart.hazard.service.SafetyMaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 安全资料管理 — 同业电力（tongye）前端 build/safetyMaterial/*
 */
@RestController
@RequestMapping("/build/safetyMaterial")
@RequiredArgsConstructor
public class SafetyMaterialController {

    private final SafetyMaterialService safetyMaterialService;

    // ===== 资料管理 =====

    @PostMapping("/selectPageAllByPid")
    public Result<PageResult<SafetyMaterial>> selectPageAllByPid(@RequestBody Map<String, Object> params) {
        return Result.ok(safetyMaterialService.selectPageAllByPid(params));
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody SafetyMaterial entity) {
        safetyMaterialService.add(entity);
        return Result.ok();
    }

    @PostMapping("/getById/{id}")
    public Result<SafetyMaterial> getById(@PathVariable Long id) {
        return Result.ok(safetyMaterialService.getById(id));
    }

    @PostMapping("/removeById/{id}")
    public Result<Void> removeById(@PathVariable Long id) {
        safetyMaterialService.removeById(id);
        return Result.ok();
    }

    @PostMapping("/selectByCollect/{projectId}")
    public Result<PageResult<SafetyMaterial>> selectByCollect(@PathVariable Long projectId) {
        return Result.ok(safetyMaterialService.selectByCollect(projectId));
    }

    @PostMapping("/collect/{id}")
    public Result<Void> collect(@PathVariable Long id) {
        safetyMaterialService.collect(id);
        return Result.ok();
    }

    @PostMapping("/deleteCollect/{id}")
    public Result<Void> deleteCollect(@PathVariable Long id) {
        safetyMaterialService.deleteCollect(id);
        return Result.ok();
    }
}
