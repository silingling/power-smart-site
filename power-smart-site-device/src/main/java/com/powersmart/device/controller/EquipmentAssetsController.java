package com.powersmart.device.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.device.entity.EquipmentAssets;
import com.powersmart.device.mapper.EquipmentAssetsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 设备资产管理 — 同业电力前端 build/equipmentAssets/*
 */
@RestController
@RequestMapping("/build/equipmentAssets")
@RequiredArgsConstructor
public class EquipmentAssetsController {

    private final EquipmentAssetsMapper mapper;

    @PostMapping("/list")
    public Result<PageResult<EquipmentAssets>> list(@RequestBody(required = false) Map<String, Object> params) {
        Page<EquipmentAssets> page = extractPage(params);
        LambdaQueryWrapper<EquipmentAssets> wrapper = new LambdaQueryWrapper<>();
        if (params != null) {
            if (params.containsKey("projectId") && params.get("projectId") != null)
                wrapper.eq(EquipmentAssets::getProjectId, Long.valueOf(params.get("projectId").toString()));
            if (params.containsKey("deviceType") && params.get("deviceType") != null && StrUtil.isNotBlank(params.get("deviceType").toString()))
                wrapper.eq(EquipmentAssets::getDeviceType, params.get("deviceType").toString());
        }
        wrapper.orderByDesc(EquipmentAssets::getCreateTime);
        return Result.ok(PageResult.from(mapper.selectPage(page, wrapper)));
    }

    @PostMapping("/selectVideoMonitor")
    public Result<List<EquipmentAssets>> selectVideoMonitor(@RequestBody Map<String, Object> params) {
        LambdaQueryWrapper<EquipmentAssets> wrapper = new LambdaQueryWrapper<EquipmentAssets>()
                .gt(EquipmentAssets::getVideoMonitorId, 0);
        if (params != null && params.containsKey("projectId") && params.get("projectId") != null)
            wrapper.eq(EquipmentAssets::getProjectId, Long.valueOf(params.get("projectId").toString()));
        return Result.ok(mapper.selectList(wrapper));
    }

    @PostMapping("/getDeviceId/{id}")
    public Result<EquipmentAssets> getDeviceId(@PathVariable Long id) {
        return Result.ok(mapper.selectById(id));
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody EquipmentAssets entity) {
        mapper.insert(entity);
        return Result.ok();
    }

    @PostMapping("/edit")
    public Result<Void> edit(@RequestBody EquipmentAssets entity) {
        mapper.updateById(entity);
        return Result.ok();
    }

    @PostMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        mapper.deleteById(id);
        return Result.ok();
    }

    @SuppressWarnings("unchecked")
    private Page<EquipmentAssets> extractPage(Map<String, Object> params) {
        int p = 1, s = 20;
        if (params != null) {
            if (params.get("page") != null) try { p = Integer.parseInt(params.get("page").toString()); } catch (NumberFormatException ignored) {}
            if (params.get("limit") != null) try { s = Integer.parseInt(params.get("limit").toString()); } catch (NumberFormatException ignored) {}
            if (params.get("pageSize") != null) try { s = Integer.parseInt(params.get("pageSize").toString()); } catch (NumberFormatException ignored) {}
        }
        return new Page<>(p, s);
    }
}
