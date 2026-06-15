package com.powersmart.hazard.controller;

import com.powersmart.common.entity.Result;
import com.powersmart.hazard.entity.QualMaterialChangelog;
import com.powersmart.hazard.service.QualMaterialChangelogService;
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

    private final QualMaterialChangelogService qualMaterialChangelogService;

    @PostMapping("/add")
    public Result<Void> add(@RequestBody QualMaterialChangelog entity) {
        qualMaterialChangelogService.add(entity);
        return Result.ok();
    }

    @PostMapping("/selectByPid/{pid}")
    public Result<List<QualMaterialChangelog>> selectByPid(@PathVariable Long pid) {
        return Result.ok(qualMaterialChangelogService.selectByPid(pid));
    }
}
