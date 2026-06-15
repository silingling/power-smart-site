package com.powersmart.hazard.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.hazard.entity.SafetyMaterial;
import com.powersmart.hazard.mapper.SafetyMaterialMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 质量资料管理 — 同业电力前端 build/qualMaterial/*
 * 复用安全资料的存储表，通过 catalogId 范围隔离（预留扩展为独立表）
 */
@RestController
@RequestMapping("/build/qualMaterial")
@RequiredArgsConstructor
public class QualMaterialController {

    private final SafetyMaterialMapper mapper;

    @PostMapping("/selectPageList")
    public Result<PageResult<SafetyMaterial>> selectPageList(@RequestBody(required = false) Map<String, Object> params) {
        int p = 1, s = 20;
        if (params != null) {
            try { if (params.get("page") != null) p = Integer.parseInt(params.get("page").toString()); } catch (Exception ignored) {}
            try { if (params.get("pageSize") != null) s = Math.min(Integer.parseInt(params.get("pageSize").toString()), 200); } catch (Exception ignored) {}
        }
        Page<SafetyMaterial> page = new Page<>(p, s);
        LambdaQueryWrapper<SafetyMaterial> wrapper = new LambdaQueryWrapper<>();
        if (params != null) {
            if (params.containsKey("projectId") && params.get("projectId") != null)
                wrapper.eq(SafetyMaterial::getProjectId, Long.valueOf(params.get("projectId").toString()));
            if (params.containsKey("catalogId") && params.get("catalogId") != null)
                wrapper.eq(SafetyMaterial::getCatalogId, Long.valueOf(params.get("catalogId").toString()));
        }
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

    @PostMapping("/qualMaterialCatalog/add")
    public Result<Void> catalogAdd(@RequestBody Map<String, Object> params) {
        // 目录管理 — 按需扩展
        return Result.ok();
    }

    @PostMapping("/qualMaterialCatalog/selectTree/{projectId}")
    public Result<List<Map<String, Object>>> catalogSelectTree(@PathVariable Long projectId) {
        return Result.ok(new ArrayList<>());
    }
}
