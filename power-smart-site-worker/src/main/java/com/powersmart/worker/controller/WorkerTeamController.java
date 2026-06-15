package com.powersmart.worker.controller;

import com.powersmart.common.entity.Result;
import com.powersmart.worker.entity.WorkerTeam;
import com.powersmart.worker.mapper.WorkerTeamMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 施工班组管理
 */
@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
public class WorkerTeamController {

    private final WorkerTeamMapper teamMapper;

    @PostMapping
    public Result<WorkerTeam> create(@RequestBody WorkerTeam team) {
        teamMapper.insert(team);
        return Result.ok(team);
    }

    @GetMapping
    public Result<List<WorkerTeam>> list(@RequestParam Long projectId) {
        return Result.ok(teamMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<WorkerTeam>()
                        .eq(WorkerTeam::getProjectId, projectId)));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody WorkerTeam team) {
        team.setId(id);
        teamMapper.updateById(team);
        return Result.ok();
    }
}
