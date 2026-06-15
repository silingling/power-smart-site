package com.powersmart.progress.controller;

import com.powersmart.common.entity.Result;
import com.powersmart.progress.entity.ProgressTask;
import com.powersmart.progress.entity.ProgressReport;
import com.powersmart.progress.service.ProgressReportService;
import com.powersmart.progress.service.ProgressTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressTaskService taskService;
    private final ProgressReportService reportService;

    // ====== 工序管理 ======
    @PostMapping("/tasks")
    public Result<ProgressTask> createTask(@RequestBody ProgressTask task) {
        taskService.save(task);
        return Result.ok(task);
    }

    @GetMapping("/tasks")
    public Result<List<ProgressTask>> getTasks(@RequestParam Long projectId) {
        return Result.ok(taskService.getTaskTree(projectId));
    }

    @GetMapping("/tasks/delayed")
    public Result<List<ProgressTask>> getDelayed(@RequestParam Long projectId) {
        return Result.ok(taskService.getDelayedTasks(projectId));
    }

    // ====== 进度上报 ======
    @PostMapping("/reports")
    public Result<ProgressReport> submitReport(@RequestBody ProgressReport report) {
        reportService.save(report);
        // 更新工序完成率
        ProgressTask task = taskService.getById(report.getTaskId());
        if (task != null) {
            task.setActualCompletionRate(report.getCompletionRate());
            taskService.updateById(task);
        }
        return Result.ok(report);
    }

    @GetMapping("/reports")
    public Result<List<ProgressReport>> getReports(
            @RequestParam Long taskId,
            @RequestParam(required = false) String date) {
        return Result.ok(reportService.lambdaQuery()
                .eq(ProgressReport::getTaskId, taskId)
                .orderByDesc(ProgressReport::getCreatedAt)
                .list());
    }
}
