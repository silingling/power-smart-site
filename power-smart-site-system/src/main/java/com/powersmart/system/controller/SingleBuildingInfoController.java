package com.powersmart.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.powersmart.common.entity.Result;
import com.powersmart.system.entity.SingleBuildingInfo;
import com.powersmart.system.mapper.SingleBuildingInfoMapper;
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

    private final SingleBuildingInfoMapper mapper;

    @PostMapping("/add")
    public Result<Void> add(@RequestBody SingleBuildingInfo entity) {
        mapper.insert(entity);
        return Result.ok();
    }

    @PostMapping("/selectById/{id}")
    public Result<SingleBuildingInfo> selectById(@PathVariable Long id) {
        return Result.ok(mapper.selectById(id));
    }

    @PostMapping("/selectByProjectId/{projectId}")
    public Result<List<SingleBuildingInfo>> selectByProjectId(@PathVariable Long projectId) {
        List<SingleBuildingInfo> list = mapper.selectList(
                new LambdaQueryWrapper<SingleBuildingInfo>()
                        .eq(SingleBuildingInfo::getProjectId, projectId)
                        .orderByDesc(SingleBuildingInfo::getCreateTime));
        return Result.ok(list);
    }

    @PostMapping("/removeById/{id}")
    public Result<Void> removeById(@PathVariable Long id) {
        mapper.deleteById(id);
        return Result.ok();
    }
}
