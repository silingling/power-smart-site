package com.powersmart.hazard.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.powersmart.common.entity.Result;
import com.powersmart.hazard.entity.SafetyMaterialChangelog;
import com.powersmart.hazard.mapper.SafetyMaterialChangelogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 安全资料变更日志 — 同业电力前端 build/safetyMaterialChangelog/*
 */
@RestController
@RequestMapping("/build/safetyMaterialChangelog")
@RequiredArgsConstructor
public class SafetyMaterialChangelogController {

    private final SafetyMaterialChangelogMapper mapper;

    @PostMapping("/add")
    public Result<Void> add(@RequestBody SafetyMaterialChangelog entity) {
        mapper.insert(entity);
        return Result.ok();
    }

    @PostMapping("/selectByPid/{materialId}")
    public Result<List<SafetyMaterialChangelog>> selectByPid(@PathVariable Long materialId) {
        List<SafetyMaterialChangelog> list = mapper.selectList(
                new LambdaQueryWrapper<SafetyMaterialChangelog>()
                        .eq(SafetyMaterialChangelog::getMaterialId, materialId)
                        .orderByDesc(SafetyMaterialChangelog::getCreateTime));
        return Result.ok(list);
    }
}
