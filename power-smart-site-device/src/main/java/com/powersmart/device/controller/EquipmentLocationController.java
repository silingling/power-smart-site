package com.powersmart.device.controller;

import com.powersmart.common.entity.Result;
import com.powersmart.device.entity.EquipmentLocation;
import com.powersmart.device.service.EquipmentLocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 设备位置台账树 — 对接同业电力（tongye）前端 build/equipmentLocation/*
 */
@RestController
@RequestMapping("/build/equipmentLocation")
@RequiredArgsConstructor
public class EquipmentLocationController {

    private final EquipmentLocationService equipmentLocationService;

    @PostMapping("/queryTreeListByParentId/{parentId}")
    public Result<List<EquipmentLocation>> queryTreeListByParentId(
            @PathVariable Long parentId, @RequestBody(required = false) EquipmentLocation query) {
        return Result.ok(equipmentLocationService.queryTreeListByParentId(parentId, query));
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody EquipmentLocation entity) {
        equipmentLocationService.add(entity);
        return Result.ok();
    }

    @PostMapping("/edit")
    public Result<Void> edit(@RequestBody EquipmentLocation entity) {
        equipmentLocationService.edit(entity);
        return Result.ok();
    }

    @PostMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        equipmentLocationService.delete(id);
        return Result.ok();
    }

    @PostMapping("/list")
    public Result<List<Map<String, Object>>> list() {
        return Result.ok(equipmentLocationService.list());
    }
}
