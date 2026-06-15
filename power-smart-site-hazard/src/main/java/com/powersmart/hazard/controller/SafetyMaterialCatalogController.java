package com.powersmart.hazard.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.powersmart.common.entity.Result;
import com.powersmart.hazard.entity.SafetyMaterialCatalog;
import com.powersmart.hazard.mapper.SafetyMaterialCatalogMapper;
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

    private final SafetyMaterialCatalogMapper catalogMapper;

    @PostMapping("/add")
    public Result<Void> add(@RequestBody SafetyMaterialCatalog entity) {
        catalogMapper.insert(entity);
        return Result.ok();
    }

    @PostMapping("/selectTree/{projectId}")
    public Result<List<SafetyMaterialCatalog>> selectTree(@PathVariable Long projectId) {
        return Result.ok(catalogMapper.selectList(
                new LambdaQueryWrapper<SafetyMaterialCatalog>()
                        .eq(SafetyMaterialCatalog::getProjectId, projectId)
                        .orderByAsc(SafetyMaterialCatalog::getSortOrder)));
    }

    @PostMapping("/selectById/{id}")
    public Result<SafetyMaterialCatalog> selectById(@PathVariable Long id) {
        return Result.ok(catalogMapper.selectById(id));
    }

    @PostMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        catalogMapper.deleteById(id);
        return Result.ok();
    }

    @PostMapping("/deleteOneselfAndSublevel/{id}")
    public Result<Void> deleteWithChildren(@PathVariable Long id) {
        catalogMapper.deleteById(id);
        catalogMapper.delete(new LambdaQueryWrapper<SafetyMaterialCatalog>()
                .eq(SafetyMaterialCatalog::getParentId, id));
        return Result.ok();
    }
}
