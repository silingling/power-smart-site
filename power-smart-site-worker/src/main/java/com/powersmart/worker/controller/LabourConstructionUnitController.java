package com.powersmart.worker.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.worker.entity.LabourConstructionUnit;
import com.powersmart.worker.mapper.LabourConstructionUnitMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 建设单位管理 — 对接同业电力（tongye）前端 build/labourConstructionUnit/*
 */
@RestController
@RequestMapping("/build/labourConstructionUnit")
@RequiredArgsConstructor
public class LabourConstructionUnitController {

    private final LabourConstructionUnitMapper mapper;

    @SuppressWarnings("unchecked")
    private Page<LabourConstructionUnit> extractPage(Map<String, Object> params) {
        int p = 1, s = 20;
        if (params != null) {
            if (params.containsKey("page")) p = Integer.parseInt(params.get("page").toString());
            if (params.containsKey("limit")) s = Integer.parseInt(params.get("limit").toString());
            if (params.containsKey("pageSize")) s = Integer.parseInt(params.get("pageSize").toString());
        }
        return new Page<>(p, s);
    }

    @PostMapping("/selectPageList")
    public Result<PageResult<LabourConstructionUnit>> selectPageList(@RequestBody(required = false) Map<String, Object> params) {
        Page<LabourConstructionUnit> page = extractPage(params);
        LambdaQueryWrapper<LabourConstructionUnit> wrapper = new LambdaQueryWrapper<>();
        if (params != null) {
            if (params.containsKey("unitName")) {
                wrapper.like(LabourConstructionUnit::getUnitName, params.get("unitName"));
            }
        }
        wrapper.orderByDesc(LabourConstructionUnit::getCreatedAt);
        return Result.ok(PageResult.from(mapper.selectPage(page, wrapper)));
    }

    @PostMapping("/getById/{id}")
    public Result<LabourConstructionUnit> getById(@PathVariable Long id) {
        return Result.ok(mapper.selectById(id));
    }

    @PostMapping("/save")
    public Result<Void> save(@RequestBody LabourConstructionUnit entity) {
        if (entity.getId() != null) {
            mapper.updateById(entity);
        } else {
            mapper.insert(entity);
        }
        return Result.ok();
    }

    @PostMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        mapper.deleteById(id);
        return Result.ok();
    }
}
