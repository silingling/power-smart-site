package com.powersmart.progress.controller;

import com.powersmart.common.annotation.OperateLog;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.progress.entity.ConstructionLog;
import com.powersmart.progress.service.ConstructionLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/build/constructionLog")
@RequiredArgsConstructor
public class ConstructionLogController {

    private final ConstructionLogService constructionLogService;

    @PostMapping("/list")
    public Result<PageResult<ConstructionLog>> list(@RequestBody(required = false) Map<String, Object> params) {
        return Result.ok(constructionLogService.list(params));
    }

    @PostMapping("/get/{id}")
    public Result<ConstructionLog> get(@PathVariable Long id) {
        return Result.ok(constructionLogService.getById(id));
    }

    @PostMapping("/add")
    @OperateLog(module = "施工日志", action = "add", description = "新增施工日志")
    public Result<ConstructionLog> add(@RequestBody ConstructionLog entity) {
        return Result.ok(constructionLogService.add(entity));
    }

    @PostMapping("/set")
    @OperateLog(module = "施工日志", action = "update", description = "更新施工日志")
    public Result<ConstructionLog> set(@RequestBody ConstructionLog entity) {
        return Result.ok(constructionLogService.update(entity));
    }

    @PostMapping("/del/{id}")
    @OperateLog(module = "施工日志", action = "delete", description = "删除施工日志")
    public Result<Void> del(@PathVariable Long id) {
        constructionLogService.delete(id);
        return Result.ok();
    }

    @PostMapping("/submit")
    @OperateLog(module = "施工日志", action = "submit", description = "提交施工日志")
    public Result<ConstructionLog> submit(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        return Result.ok(constructionLogService.submit(id));
    }

    @PostMapping("/approve")
    @OperateLog(module = "施工日志", action = "approve", description = "审批施工日志")
    public Result<ConstructionLog> approve(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Long signatoryId = Long.valueOf(params.get("signatoryId").toString());
        String signatoryName = (String) params.get("signatoryName");
        return Result.ok(constructionLogService.approve(id, signatoryId, signatoryName));
    }

    @PostMapping("/getByDate")
    public Result<ConstructionLog> getByDate(@RequestBody Map<String, Object> params) {
        Long projectId = Long.valueOf(params.get("projectId").toString());
        LocalDate date = LocalDate.parse((String) params.get("date"));
        return Result.ok(constructionLogService.getByDate(projectId, date));
    }

    @PostMapping("/getStats")
    public Result<Map<String, Object>> getStats(@RequestBody Map<String, Object> params) {
        Long projectId = Long.valueOf(params.get("projectId").toString());
        return Result.ok(constructionLogService.getLogStats(projectId));
    }
}
