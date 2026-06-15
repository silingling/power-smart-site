package com.powersmart.hazard.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.powersmart.common.entity.Result;
import com.powersmart.hazard.entity.SafetyMaterialCatalog;
import com.powersmart.hazard.mapper.SafetyMaterialCatalogMapper;
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

    private final SafetyMaterialCatalogMapper catalogMapper;

    @PostMapping("/add")
    public Result<Void> add(@RequestBody SafetyMaterialCatalog entity) {
        catalogMapper.insert(entity);
        return Result.ok();
    }

    @PostMapping("/selectTree/{projectId}")
    public Result<List<Map<String, Object>>> selectTree(@PathVariable Long projectId) {
        List<Map<String, Object>> result = new ArrayList<>();
        List<SafetyMaterialCatalog> roots = catalogMapper.selectList(
                new LambdaQueryWrapper<SafetyMaterialCatalog>()
                        .eq(SafetyMaterialCatalog::getProjectId, projectId)
                        .eq(SafetyMaterialCatalog::getParentId, 0));
        for (SafetyMaterialCatalog r : roots) {
            Map<String, Object> node = catalogToMap(r);
            node.put("children", buildChildren(r.getId()));
            result.add(node);
        }
        return Result.ok(result);
    }

    @PostMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        catalogMapper.deleteById(id);
        return Result.ok();
    }

    @PostMapping("/selectById/{id}")
    public Result<SafetyMaterialCatalog> selectById(@PathVariable Long id) {
        return Result.ok(catalogMapper.selectById(id));
    }

    // ---- helpers ----

    private List<Map<String, Object>> buildChildren(Long parentId) {
        List<Map<String, Object>> children = new ArrayList<>();
        List<SafetyMaterialCatalog> list = catalogMapper.selectList(
                new LambdaQueryWrapper<SafetyMaterialCatalog>().eq(SafetyMaterialCatalog::getParentId, parentId));
        for (SafetyMaterialCatalog c : list) {
            Map<String, Object> node = catalogToMap(c);
            node.put("children", buildChildren(c.getId()));
            children.add(node);
        }
        return children;
    }

    private Map<String, Object> catalogToMap(SafetyMaterialCatalog c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.getId());
        m.put("name", c.getName());
        m.put("parentId", c.getParentId());
        m.put("projectId", c.getProjectId());
        return m;
    }
}
