package com.powersmart.hazard.controller;

import com.powersmart.common.entity.Result;
import com.powersmart.hazard.entity.SafetyMaterialCatalog;
import com.powersmart.hazard.service.QualMaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 质量资料目录管理 — 同业电力前端 build/qualMaterialCatalog/*
 * 复用安全资料的目录表 (safety_material_catalog)
 */
@RestController
@RequestMapping("/build/qualMaterialCatalog")
@RequiredArgsConstructor
public class QualMaterialCatalogController {

    private final QualMaterialService qualMaterialService;

    @PostMapping("/add")
    public Result<Void> add(@RequestBody SafetyMaterialCatalog entity) {
        qualMaterialService.addCatalog(entity);
        return Result.ok();
    }

    @PostMapping("/selectTree/{projectId}")
    public Result<List<Map<String, Object>>> selectTree(@PathVariable Long projectId) {
        return Result.ok(qualMaterialService.selectCatalogTree(projectId));
    }

    @PostMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        qualMaterialService.deleteCatalog(id);
        return Result.ok();
    }

    @PostMapping("/selectById/{id}")
    public Result<SafetyMaterialCatalog> selectById(@PathVariable Long id) {
        return Result.ok(qualMaterialService.getCatalogById(id));
    }
}
