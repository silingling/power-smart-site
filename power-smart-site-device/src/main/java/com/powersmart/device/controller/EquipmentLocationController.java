package com.powersmart.device.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.powersmart.common.entity.Result;
import com.powersmart.device.entity.EquipmentLocation;
import com.powersmart.device.mapper.EquipmentLocationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 设备位置台账树 — 对接同业电力（tongye）前端 build/equipmentLocation/*
 */
@RestController
@RequestMapping("/build/equipmentLocation")
@RequiredArgsConstructor
public class EquipmentLocationController {

    private final EquipmentLocationMapper mapper;

    @PostMapping("/queryTreeListByParentId/{parentId}")
    public Result<List<EquipmentLocation>> queryTreeListByParentId(
            @PathVariable Long parentId, @RequestBody(required = false) EquipmentLocation query) {
        LambdaQueryWrapper<EquipmentLocation> wrapper = new LambdaQueryWrapper<EquipmentLocation>()
                .eq(parentId != null, EquipmentLocation::getParentId, parentId != null ? parentId : 0)
                .orderByAsc(EquipmentLocation::getSortOrder);

        if (query != null && query.getProjectId() != null) {
            wrapper.eq(EquipmentLocation::getProjectId, query.getProjectId());
        }
        return Result.ok(mapper.selectList(wrapper));
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody EquipmentLocation entity) {
        mapper.insert(entity);
        return Result.ok();
    }

    @PostMapping("/edit")
    public Result<Void> edit(@RequestBody EquipmentLocation entity) {
        mapper.updateById(entity);
        return Result.ok();
    }

    @PostMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        mapper.deleteById(id);
        return Result.ok();
    }

    @PostMapping("/list")
    public Result<List<Map<String, Object>>> list() {
        List<EquipmentLocation> all = mapper.selectList(
                new LambdaQueryWrapper<EquipmentLocation>().orderByAsc(EquipmentLocation::getSortOrder));
        // 构建树形结构
        List<Map<String, Object>> tree = buildTree(all, 0L);
        return Result.ok(tree);
    }

    private List<Map<String, Object>> buildTree(List<EquipmentLocation> all, Long parentId) {
        return all.stream()
                .filter(n -> n.getParentId() != null && n.getParentId().equals(parentId))
                .map(n -> {
                    Map<String, Object> node = new java.util.LinkedHashMap<>();
                    node.put("id", n.getId());
                    node.put("label", n.getLocationName());
                    node.put("locationType", n.getLocationType());
                    List<Map<String, Object>> children = buildTree(all, n.getId());
                    if (!children.isEmpty()) {
                        node.put("children", children);
                    }
                    return node;
                }).collect(Collectors.toList());
    }
}
