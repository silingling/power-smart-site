package com.powersmart.worker.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.worker.entity.WorkerTeam;
import com.powersmart.worker.mapper.WorkerTeamMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 施工班组 — 同业电力（tongye）前端 build/labourTeam/*
 */
@RestController
@RequestMapping("/build/labourTeam")
@RequiredArgsConstructor
public class LabourTeamController {

    private final WorkerTeamMapper teamMapper;

    @PostMapping("/list")
    public Result<PageResult<WorkerTeam>> list(@RequestBody(required = false) Map<String, Object> params) {
        Page<WorkerTeam> page = extractPage(params);
        LambdaQueryWrapper<WorkerTeam> wrapper = new LambdaQueryWrapper<>();
        if (params != null) {
            if (params.containsKey("projectId") && params.get("projectId") != null)
                wrapper.eq(WorkerTeam::getProjectId, Long.valueOf(params.get("projectId").toString()));
            if (params.containsKey("teamName") && params.get("teamName") != null)
                wrapper.like(WorkerTeam::getTeamName, params.get("teamName").toString());
        }
        wrapper.orderByDesc(WorkerTeam::getCreatedAt);
        return Result.ok(PageResult.from(teamMapper.selectPage(page, wrapper)));
    }

    @PostMapping("/queryById/{id}")
    public Result<WorkerTeam> queryById(@PathVariable Long id) {
        return Result.ok(teamMapper.selectById(id));
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody WorkerTeam team) {
        teamMapper.insert(team);
        return Result.ok();
    }

    @PostMapping("/edit")
    public Result<Void> edit(@RequestBody WorkerTeam team) {
        teamMapper.updateById(team);
        return Result.ok();
    }

    @PostMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        teamMapper.deleteById(id);
        return Result.ok();
    }

    @PostMapping("/selcetIdsAndName")
    public Result<List<Map<String, Object>>> selcetIdsAndName(@RequestBody(required = false) Map<String, Object> params) {
        LambdaQueryWrapper<WorkerTeam> wrapper = new LambdaQueryWrapper<>();
        if (params != null && params.containsKey("projectId") && params.get("projectId") != null)
            wrapper.eq(WorkerTeam::getProjectId, Long.valueOf(params.get("projectId").toString()));
        wrapper.eq(WorkerTeam::getStatus, 1);
        List<WorkerTeam> list = teamMapper.selectList(wrapper);
        List<Map<String, Object>> result = list.stream().map(t -> {
            Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("id", t.getId());
            m.put("teamName", t.getTeamName());
            return m;
        }).collect(Collectors.toList());
        return Result.ok(result);
    }

    @SuppressWarnings("unchecked")
    private Page<WorkerTeam> extractPage(Map<String, Object> params) {
        int p = 1, s = 20;
        if (params != null) {
            if (params.containsKey("page")) p = Integer.parseInt(params.get("page").toString());
            if (params.containsKey("limit")) s = Integer.parseInt(params.get("limit").toString());
            if (params.containsKey("pageSize")) s = Integer.parseInt(params.get("pageSize").toString());
        }
        return new Page<>(p, s);
    }
}
