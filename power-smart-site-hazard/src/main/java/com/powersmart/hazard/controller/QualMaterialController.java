package com.powersmart.hazard.controller;

import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.hazard.entity.SafetyMaterial;
import com.powersmart.hazard.service.QualMaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 质量资料管理 — 同业电力前端 build/qualMaterial/*
 * 复用安全资料的存储表
 */
@RestController
@RequestMapping("/build/qualMaterial")
@RequiredArgsConstructor
public class QualMaterialController {

    private final QualMaterialService qualMaterialService;

    @PostMapping("/selectPageList")
    public Result<PageResult<SafetyMaterial>> selectPageList(@RequestBody(required = false) Map<String, Object> params) {
        return Result.ok(qualMaterialService.selectPageList(params));
    }

    @PostMapping("/selectPageAllByPid")
    public Result<PageResult<SafetyMaterial>> selectPageAllByPid(@RequestBody Map<String, Object> params) {
        return selectPageList(params);
    }

    @PostMapping("/selectByCollect/{projectId}")
    public Result<List<SafetyMaterial>> selectByCollect(@PathVariable Long projectId) {
        return Result.ok(qualMaterialService.selectByCollect(projectId));
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody SafetyMaterial entity) {
        qualMaterialService.add(entity);
        return Result.ok();
    }

    @PostMapping("/getById/{id}")
    public Result<SafetyMaterial> getById(@PathVariable Long id) {
        return Result.ok(qualMaterialService.getById(id));
    }

    @PostMapping("/removeById/{id}")
    public Result<Void> removeById(@PathVariable Long id) {
        qualMaterialService.removeById(id);
        return Result.ok();
    }

    @PostMapping("/collect/{id}")
    public Result<Void> collect(@PathVariable Long id) {
        qualMaterialService.collect(id);
        return Result.ok();
    }

    @PostMapping("/deleteCollect/{id}")
    public Result<Void> deleteCollect(@PathVariable Long id) {
        qualMaterialService.deleteCollect(id);
        return Result.ok();
    }
}
