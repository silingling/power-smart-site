package com.powersmart.worker.controller;

import com.powersmart.common.entity.Result;
import com.powersmart.worker.entity.Worker;
import com.powersmart.worker.entity.WorkerCertificate;
import com.powersmart.worker.service.WorkerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/workers")
@RequiredArgsConstructor
public class WorkerController {

    private final WorkerService workerService;

    @PostMapping
    public Result<Worker> create(@RequestBody Worker worker) {
        workerService.save(worker);
        return Result.ok(worker);
    }

    @GetMapping
    public Result<List<Worker>> list(
            @RequestParam Long projectId,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) Integer status) {
        return Result.ok(workerService.getByProject(projectId, teamId, status));
    }

    @GetMapping("/{id}")
    public Result<Worker> getById(@PathVariable Long id) {
        return Result.ok(workerService.getById(id));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Worker worker) {
        worker.setId(id);
        workerService.updateById(worker);
        return Result.ok();
    }

    @PostMapping("/import")
    public Result<Integer> batchImport(@RequestBody List<Worker> workers) {
        return Result.ok(workerService.batchImport(workers));
    }

    @GetMapping("/{id}/certificates")
    public Result<List<WorkerCertificate>> getCertificates(@PathVariable Long id) {
        return Result.ok(workerService.getExpiringCertificates(30));
    }
}
