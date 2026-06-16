package com.powersmart.progress.controller;

import com.powersmart.common.annotation.OperateLog;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.progress.entity.SafetyLog;
import com.powersmart.progress.service.SafetyLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/build/safetyLog")
@RequiredArgsConstructor
public class SafetyLogController {

    private final SafetyLogService safetyLogService;

    @PostMapping("/list")
    public Result<PageResult<SafetyLog>> list(@RequestBody(required = false) Map<String, Object> params) {
        return Result.ok(safetyLogService.list(params));
    }

    @PostMapping("/get/{id}")
    public Result<SafetyLog> get(@PathVariable Long id) {
        return Result.ok(safetyLogService.getById(id));
    }

    @PostMapping("/add")
    @OperateLog(module = "安全日志", action = "add", description = "新增安全日志")
    public Result<SafetyLog> add(@RequestBody SafetyLog entity) {
        return Result.ok(safetyLogService.add(entity));
    }

    @PostMapping("/set")
    @OperateLog(module = "安全日志", action = "update", description = "更新安全日志")
    public Result<SafetyLog> set(@RequestBody SafetyLog entity) {
        return Result.ok(safetyLogService.update(entity));
    }

    @PostMapping("/del/{id}")
    @OperateLog(module = "安全日志", action = "delete", description = "删除安全日志")
    public Result<Void> del(@PathVariable Long id) {
        safetyLogService.delete(id);
        return Result.ok();
    }

    @PostMapping("/submit")
    @OperateLog(module = "安全日志", action = "submit", description = "提交安全日志")
    public Result<SafetyLog> submit(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        return Result.ok(safetyLogService.submit(id));
    }

    @PostMapping("/approve")
    @OperateLog(module = "安全日志", action = "approve", description = "审批安全日志")
    public Result<SafetyLog> approve(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Long signatoryId = Long.valueOf(params.get("signatoryId").toString());
        String signatoryName = (String) params.get("signatoryName");
        return Result.ok(safetyLogService.approve(id, signatoryId, signatoryName));
    }

    @PostMapping("/getByDate")
    public Result<SafetyLog> getByDate(@RequestBody Map<String, Object> params) {
        Long projectId = Long.valueOf(params.get("projectId").toString());
        LocalDate date = LocalDate.parse((String) params.get("date"));
        return Result.ok(safetyLogService.getByDate(projectId, date));
    }

    @PostMapping("/getStats")
    public Result<Map<String, Object>> getStats(@RequestBody Map<String, Object> params) {
        Long projectId = Long.valueOf(params.get("projectId").toString());
        return Result.ok(safetyLogService.getLogStats(projectId));
    }
}
