package com.powersmart.worker.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.worker.entity.WorkerTeam;
import com.powersmart.worker.service.WorkerTeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 施工班组 — 同业电力（tongye）前端 build/labourTeam/*
 */
@RestController
@RequestMapping("/build/labourTeam")
@RequiredArgsConstructor
public class LabourTeamController {

    private final WorkerTeamService workerTeamService;

    @PostMapping("/list")
    public Result<PageResult<WorkerTeam>> list(@RequestBody(required = false) Map<String, Object> params) {
        Page<WorkerTeam> page = workerTeamService.queryPage(params);
        return Result.ok(PageResult.from(page));
    }

    @PostMapping("/queryById/{id}")
    public Result<WorkerTeam> queryById(@PathVariable Long id) {
        return Result.ok(workerTeamService.getById(id));
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody WorkerTeam team) {
        workerTeamService.add(team);
        return Result.ok();
    }

    @PostMapping("/edit")
    public Result<Void> edit(@RequestBody WorkerTeam team) {
        workerTeamService.update(team);
        return Result.ok();
    }

    @PostMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        workerTeamService.delete(id);
        return Result.ok();
    }

    @PostMapping("/selcetIdsAndName")
    public Result<List<Map<String, Object>>> selcetIdsAndName(@RequestBody(required = false) Map<String, Object> params) {
        return Result.ok(workerTeamService.selectIdsAndName(params));
    }
}
