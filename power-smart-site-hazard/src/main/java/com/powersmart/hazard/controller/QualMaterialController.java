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
 * 复用安全资料的存储表
 */
@RestController
@RequestMapping("/build/qualMaterial")
@RequiredArgsConstructor
public class QualMaterialController {

    private final SafetyMaterialMapper mapper;

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
