package com.powersmart.system.controller;

import com.powersmart.common.entity.Result;
import com.powersmart.system.entity.SingleBuildingInfo;
import com.powersmart.system.service.SingleBuildingInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 单体楼栋信息 — 同业电力前端 build/singleBuildingInfo/*
 */
@RestController
@RequestMapping("/build/singleBuildingInfo")
@RequiredArgsConstructor
public class SingleBuildingInfoController {

    private final SingleBuildingInfoService singleBuildingInfoService;

    @PostMapping("/add")
    public Result<Void> add(@RequestBody SingleBuildingInfo entity) {
        singleBuildingInfoService.add(entity);
        return Result.ok();
    }

    @PostMapping("/selectById/{id}")
    public Result<SingleBuildingInfo> selectById(@PathVariable Long id) {
        return Result.ok(singleBuildingInfoService.selectById(id));
    }

    @PostMapping("/selectByProjectId/{projectId}")
    public Result<List<SingleBuildingInfo>> selectByProjectId(@PathVariable Long projectId) {
        return Result.ok(singleBuildingInfoService.selectByProjectId(projectId));
    }

    @PostMapping("/removeById/{id}")
    public Result<Void> removeById(@PathVariable Long id) {
        singleBuildingInfoService.removeById(id);
        return Result.ok();
    }
}
