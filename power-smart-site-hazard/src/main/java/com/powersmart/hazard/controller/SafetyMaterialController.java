package com.powersmart.hazard.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.hazard.entity.SafetyMaterial;
import com.powersmart.hazard.entity.SafetyMaterialCatalog;
import com.powersmart.hazard.mapper.SafetyMaterialCatalogMapper;
import com.powersmart.hazard.mapper.SafetyMaterialMapper;
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

    private final SafetyMaterialMapper mapper;
    private final SafetyMaterialCatalogMapper catalogMapper;

    // ===== 资料管理 =====

    @PostMapping("/selectPageAllByPid")
    public Result<PageResult<SafetyMaterial>> selectPageAllByPid(@RequestBody Map<String, Object> params) {
        Page<SafetyMaterial> page = extractPage(params);
        LambdaQueryWrapper<SafetyMaterial> wrapper = new LambdaQueryWrapper<>();
        if (params != null) {
            if (params.containsKey("projectId") && params.get("projectId") != null)
                wrapper.eq(SafetyMaterial::getProjectId, Long.valueOf(params.get("projectId").toString()));
            if (params.containsKey("catalogId") && params.get("catalogId") != null)
                wrapper.eq(SafetyMaterial::getCatalogId, Long.valueOf(params.get("catalogId").toString()));
        }
        wrapper.orderByDesc(SafetyMaterial::getCreatedAt);
        return Result.ok(PageResult.from(mapper.selectPage(page, wrapper)));
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody SafetyMaterial entity) {
        mapper.insert(entity);
        return Result.ok();
    }

    @PostMapping("/getById/{id}")
    public Result<SafetyMaterial> getById(@PathVariable Long id) {
        return Result.ok(mapper.selectById(id));
    }

    @PostMapping("/removeById/{id}")
    public Result<Void> removeById(@PathVariable Long id) {
        mapper.deleteById(id);
        return Result.ok();
    }

    @PostMapping("/selectByCollect/{projectId}")
    public Result<PageResult<SafetyMaterial>> selectByCollect(@PathVariable Long projectId) {
        Page<SafetyMaterial> page = new Page<>(1, 20);
        LambdaQueryWrapper<SafetyMaterial> wrapper = new LambdaQueryWrapper<SafetyMaterial>()
                .eq(SafetyMaterial::getProjectId, projectId)
                .eq(SafetyMaterial::getIsCollect, 1);
        return Result.ok(PageResult.from(mapper.selectPage(page, wrapper)));
    }

    @PostMapping("/collect/{id}")
    public Result<Void> collect(@PathVariable Long id) {
        SafetyMaterial m = mapper.selectById(id);
        if (m != null) { m.setIsCollect(1); mapper.updateById(m); }
        return Result.ok();
    }

    @PostMapping("/deleteCollect/{id}")
    public Result<Void> deleteCollect(@PathVariable Long id) {
        SafetyMaterial m = mapper.selectById(id);
        if (m != null) { m.setIsCollect(0); mapper.updateById(m); }
        return Result.ok();
    }

    // ===== 目录管理 =====

    @PostMapping("/safetyMaterialCatalog/add")
    public Result<Void> catalogAdd(@RequestBody SafetyMaterialCatalog entity) {
        catalogMapper.insert(entity);
        return Result.ok();
    }

    @PostMapping("/safetyMaterialCatalog/selectTree/{projectId}")
    public Result<java.util.List<SafetyMaterialCatalog>> catalogSelectTree(@PathVariable Long projectId) {
        return Result.ok(catalogMapper.selectList(
                new LambdaQueryWrapper<SafetyMaterialCatalog>()
                        .eq(SafetyMaterialCatalog::getProjectId, projectId)
                        .orderByAsc(SafetyMaterialCatalog::getSortOrder)));
    }

    @PostMapping("/safetyMaterialCatalog/selectById/{id}")
    public Result<SafetyMaterialCatalog> catalogSelectById(@PathVariable Long id) {
        return Result.ok(catalogMapper.selectById(id));
    }

    @PostMapping("/safetyMaterialCatalog/delete/{id}")
    public Result<Void> catalogDelete(@PathVariable Long id) {
        catalogMapper.deleteById(id);
        return Result.ok();
    }

    @PostMapping("/safetyMaterialCatalog/deleteOneselfAndSublevel/{id}")
    public Result<Void> catalogDeleteWithChildren(@PathVariable Long id) {
        catalogMapper.deleteById(id);
        catalogMapper.delete(new LambdaQueryWrapper<SafetyMaterialCatalog>().eq(SafetyMaterialCatalog::getParentId, id));
        return Result.ok();
    }

    @SuppressWarnings("unchecked")
    private Page<SafetyMaterial> extractPage(Map<String, Object> params) {
        int p = 1, s = 20;
        if (params != null) {
            if (params.get("page") != null) try { p = Integer.parseInt(params.get("page").toString()); } catch (NumberFormatException ignored) {}
            if (params.get("limit") != null) try { s = Integer.parseInt(params.get("limit").toString()); } catch (NumberFormatException ignored) {}
            if (params.get("pageSize") != null) try { s = Integer.parseInt(params.get("pageSize").toString()); } catch (NumberFormatException ignored) {}
        }
        return new Page<>(p, s);
    }
}
