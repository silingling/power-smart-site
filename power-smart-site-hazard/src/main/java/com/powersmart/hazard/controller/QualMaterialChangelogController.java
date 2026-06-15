package com.powersmart.hazard.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.powersmart.common.entity.Result;
import com.powersmart.hazard.entity.QualMaterialChangelog;
import com.powersmart.hazard.mapper.QualMaterialChangelogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 质量资料变更日志 — 同业电力前端 build/qualMaterialChangelog/*
 */
@RestController
@RequestMapping("/build/qualMaterialChangelog")
@RequiredArgsConstructor
public class QualMaterialChangelogController {

    private final QualMaterialChangelogMapper mapper;

    @PostMapping("/add")
    public Result<Void> add(@RequestBody QualMaterialChangelog entity) {
        mapper.insert(entity);
        return Result.ok();
    }

    @PostMapping("/selectByPid/{pid}")
    public Result<List<QualMaterialChangelog>> selectByPid(@PathVariable Long pid) {
        List<QualMaterialChangelog> list = mapper.selectList(
                new LambdaQueryWrapper<QualMaterialChangelog>()
                        .eq(QualMaterialChangelog::getMaterialId, pid)
                        .orderByDesc(QualMaterialChangelog::getCreateTime));
        return Result.ok(list);
    }
}
