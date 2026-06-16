package com.powersmart.progress.controller;

import com.powersmart.common.annotation.OperateLog;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.progress.entity.LogTemplate;
import com.powersmart.progress.service.LogTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/build/logTemplate")
@RequiredArgsConstructor
public class LogTemplateController {

    private final LogTemplateService logTemplateService;

    @PostMapping("/list")
    public Result<PageResult<LogTemplate>> list(@RequestBody(required = false) Map<String, Object> params) {
        return Result.ok(logTemplateService.list(params));
    }

    @PostMapping("/get/{id}")
    public Result<LogTemplate> get(@PathVariable Long id) {
        return Result.ok(logTemplateService.getById(id));
    }

    @PostMapping("/add")
    @OperateLog(module = "日志模板", action = "add", description = "新增日志模板")
    public Result<LogTemplate> add(@RequestBody LogTemplate entity) {
        return Result.ok(logTemplateService.add(entity));
    }

    @PostMapping("/set")
    @OperateLog(module = "日志模板", action = "update", description = "更新日志模板")
    public Result<LogTemplate> set(@RequestBody LogTemplate entity) {
        return Result.ok(logTemplateService.update(entity));
    }

    @PostMapping("/del/{id}")
    @OperateLog(module = "日志模板", action = "delete", description = "删除日志模板")
    public Result<Void> del(@PathVariable Long id) {
        logTemplateService.delete(id);
        return Result.ok();
    }
}
