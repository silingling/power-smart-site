package com.powersmart.hazard.controller;

import com.powersmart.common.entity.Result;
import com.powersmart.hazard.entity.SafetyMaterialChangelog;
import com.powersmart.hazard.service.SafetyMaterialChangelogService;
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

    private final SafetyMaterialChangelogService safetyMaterialChangelogService;

    @PostMapping("/add")
    public Result<Void> add(@RequestBody SafetyMaterialChangelog entity) {
        safetyMaterialChangelogService.add(entity);
        return Result.ok();
    }

    @PostMapping("/selectByPid/{pid}")
    public Result<List<SafetyMaterialChangelog>> selectByPid(@PathVariable Long pid) {
        return Result.ok(safetyMaterialChangelogService.selectByPid(pid));
    }
}
