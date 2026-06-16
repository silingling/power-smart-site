package com.powersmart.system.controller;

import com.powersmart.common.annotation.OperateLog;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.system.entity.NotificationTemplate;
import com.powersmart.system.service.NotificationTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/build/notificationTemplate")
public class NotificationTemplateController {

    private final NotificationTemplateService templateService;

    @PostMapping("/list")
    public Result<PageResult<NotificationTemplate>> list(@RequestBody(required = false) Map<String, Object> params) {
        return Result.ok(templateService.list(params));
    }

    @PostMapping("/get/{id}")
    public Result<NotificationTemplate> getById(@PathVariable Long id) {
        return Result.ok(templateService.getById(id));
    }

    @PostMapping("/add")
    @OperateLog(module = "通知模板", action = "add", description = "新增通知模板")
    public Result<Void> add(@RequestBody NotificationTemplate template) {
        templateService.add(template);
        return Result.ok();
    }

    @PostMapping("/set")
    @OperateLog(module = "通知模板", action = "update", description = "更新通知模板")
    public Result<Void> set(@RequestBody NotificationTemplate template) {
        templateService.update(template);
        return Result.ok();
    }

    @PostMapping("/del/{id}")
    @OperateLog(module = "通知模板", action = "delete", description = "删除通知模板")
    public Result<Void> del(@PathVariable Long id) {
        templateService.delete(id);
        return Result.ok();
    }
}
