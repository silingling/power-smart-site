package com.powersmart.device.controller;

import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.device.entity.EquipmentAssets;
import com.powersmart.device.service.EquipmentAssetsService;
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

    private final EquipmentAssetsService equipmentAssetsService;

    @PostMapping("/list")
    public Result<PageResult<EquipmentAssets>> list(@RequestBody(required = false) Map<String, Object> params) {
        return Result.ok(equipmentAssetsService.list(params));
    }

    @PostMapping("/selectVideoMonitor")
    public Result<List<EquipmentAssets>> selectVideoMonitor(@RequestBody Map<String, Object> params) {
        return Result.ok(equipmentAssetsService.selectVideoMonitor(params));
    }

    @PostMapping("/getDeviceId/{id}")
    public Result<EquipmentAssets> getDeviceId(@PathVariable Long id) {
        return Result.ok(equipmentAssetsService.getDeviceId(id));
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody EquipmentAssets entity) {
        equipmentAssetsService.add(entity);
        return Result.ok();
    }

    @PostMapping("/edit")
    public Result<Void> edit(@RequestBody EquipmentAssets entity) {
        equipmentAssetsService.edit(entity);
        return Result.ok();
    }

    @PostMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        equipmentAssetsService.delete(id);
        return Result.ok();
    }
}
