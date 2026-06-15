package com.powersmart.worker.controller;

import com.powersmart.common.entity.Result;
import com.powersmart.worker.entity.WorkerTeam;
import com.powersmart.worker.service.WorkerTeamService;
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

    private final WorkerTeamService workerTeamService;

    @PostMapping
    public Result<WorkerTeam> create(@RequestBody WorkerTeam team) {
        return Result.ok(workerTeamService.create(team));
    }

    @GetMapping
    public Result<List<WorkerTeam>> list(@RequestParam Long projectId) {
        return Result.ok(workerTeamService.listByProject(projectId));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody WorkerTeam team) {
        team.setId(id);
        workerTeamService.update(team);
        return Result.ok();
    }
}
