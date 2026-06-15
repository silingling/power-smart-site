package com.powersmart.hazard.controller;

import com.powersmart.common.entity.Result;
import com.powersmart.hazard.entity.SafetyMaterialCatalog;
import com.powersmart.hazard.service.SafetyMaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 安全资料目录管理 — 同业电力前端 build/safetyMaterialCatalog/*
 */
@RestController
@RequestMapping("/build/safetyMaterialCatalog")
@RequiredArgsConstructor
public class SafetyMaterialCatalogController {

    private final SafetyMaterialService safetyMaterialService;

    @PostMapping("/add")
    public Result<Void> add(@RequestBody SafetyMaterialCatalog entity) {
        safetyMaterialService.addCatalog(entity);
        return Result.ok();
    }

    @PostMapping("/selectTree/{projectId}")
    public Result<List<SafetyMaterialCatalog>> selectTree(@PathVariable Long projectId) {
        return Result.ok(safetyMaterialService.selectCatalogTree(projectId));
    }

    @PostMapping("/selectById/{id}")
    public Result<SafetyMaterialCatalog> selectById(@PathVariable Long id) {
        return Result.ok(safetyMaterialService.getCatalogById(id));
    }

    @PostMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        safetyMaterialService.deleteCatalog(id);
        return Result.ok();
    }

    @PostMapping("/deleteOneselfAndSublevel/{id}")
    public Result<Void> deleteWithChildren(@PathVariable Long id) {
        safetyMaterialService.deleteCatalogWithChildren(id);
        return Result.ok();
    }
}
