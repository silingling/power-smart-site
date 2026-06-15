package com.powersmart.hazard.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.entity.PageResult;
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

import java.util.*;

/**
 * 质量资料管理 — 同业电力前端 build/qualMaterial/*
 * 复用安全资料的存储表 + 目录表
 */
@RestController
@RequestMapping("/build/qualMaterial")
@RequiredArgsConstructor
public class QualMaterialController {

    private final SafetyMaterialMapper mapper;
    private final SafetyMaterialCatalogMapper catalogMapper;

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

    @PostMapping("/selectPageList")
    public Result<PageResult<SafetyMaterial>> selectPageList(@RequestBody(required = false) Map<String, Object> params) {
        Page<SafetyMaterial> page = extractPage(params);
        LambdaQueryWrapper<SafetyMaterial> wrapper = buildWrapper(params);
        return Result.ok(PageResult.from(mapper.selectPage(page, wrapper)));
    }

    @PostMapping("/selectPageAllByPid")
    public Result<PageResult<SafetyMaterial>> selectPageAllByPid(@RequestBody Map<String, Object> params) {
        return selectPageList(params);
    }

    @PostMapping("/selectByCollect/{projectId}")
    public Result<List<SafetyMaterial>> selectByCollect(@PathVariable Long projectId) {
        LambdaQueryWrapper<SafetyMaterial> w = new LambdaQueryWrapper<SafetyMaterial>()
                .eq(SafetyMaterial::getProjectId, projectId)
                .eq(SafetyMaterial::getIsCollect, 1)
                .orderByDesc(SafetyMaterial::getCreateTime);
        return Result.ok(mapper.selectList(w));
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

    @PostMapping("/collect/{id}")
    public Result<Void> collect(@PathVariable Long id) {
        SafetyMaterial m = mapper.selectById(id);
        if (m != null) {
            m.setIsCollect(1);
            mapper.updateById(m);
        }
        return Result.ok();
    }

    @PostMapping("/deleteCollect/{id}")
    public Result<Void> deleteCollect(@PathVariable Long id) {
        SafetyMaterial m = mapper.selectById(id);
        if (m != null) {
            m.setIsCollect(0);
            mapper.updateById(m);
        }
        return Result.ok();
    }

    @PostMapping("/qualMaterialCatalog/add")
    public Result<Void> catalogAdd(@RequestBody SafetyMaterialCatalog entity) {
        catalogMapper.insert(entity);
        return Result.ok();
    }

    @PostMapping("/qualMaterialCatalog/selectTree/{projectId}")
    public Result<List<Map<String, Object>>> catalogSelectTree(@PathVariable Long projectId) {
        List<Map<String, Object>> result = new ArrayList<>();
        List<SafetyMaterialCatalog> roots = catalogMapper.selectList(
                new LambdaQueryWrapper<SafetyMaterialCatalog>()
                        .eq(SafetyMaterialCatalog::getProjectId, projectId)
                        .eq(SafetyMaterialCatalog::getParentId, 0));
        for (SafetyMaterialCatalog r : roots) {
            Map<String, Object> node = catalogToMap(r);
            node.put("children", buildCatalogChildren(r.getId()));
            result.add(node);
        }
        return Result.ok(result);
    }

    @PostMapping("/qualMaterialCatalog/delete/{id}")
    public Result<Void> catalogDelete(@PathVariable Long id) {
        catalogMapper.deleteById(id);
        return Result.ok();
    }

    @PostMapping("/qualMaterialCatalog/selectById/{id}")
    public Result<SafetyMaterialCatalog> catalogSelectById(@PathVariable Long id) {
        return Result.ok(catalogMapper.selectById(id));
    }

    // ---- helper ----

    private List<Map<String, Object>> buildCatalogChildren(Long parentId) {
        List<Map<String, Object>> children = new ArrayList<>();
        List<SafetyMaterialCatalog> list = catalogMapper.selectList(
                new LambdaQueryWrapper<SafetyMaterialCatalog>().eq(SafetyMaterialCatalog::getParentId, parentId));
        for (SafetyMaterialCatalog c : list) {
            Map<String, Object> node = catalogToMap(c);
            node.put("children", buildCatalogChildren(c.getId()));
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

    private LambdaQueryWrapper<SafetyMaterial> buildWrapper(Map<String, Object> params) {
        LambdaQueryWrapper<SafetyMaterial> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SafetyMaterial::getIsQual, 1); // qual 标识
        if (params != null) {
            if (params.containsKey("projectId") && params.get("projectId") != null)
                wrapper.eq(SafetyMaterial::getProjectId, Long.valueOf(params.get("projectId").toString()));
            if (params.containsKey("catalogId") && params.get("catalogId") != null)
                wrapper.eq(SafetyMaterial::getCatalogId, Long.valueOf(params.get("catalogId").toString()));
        }
        wrapper.orderByDesc(SafetyMaterial::getCreateTime);
        return wrapper;
    }
}
