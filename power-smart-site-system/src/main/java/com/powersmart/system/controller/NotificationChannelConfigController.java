package com.powersmart.system.controller;

import com.powersmart.common.annotation.OperateLog;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.system.entity.NotificationChannelConfig;
import com.powersmart.system.service.NotificationChannelConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/build/notificationChannel")
public class NotificationChannelConfigController {

    private final NotificationChannelConfigService channelConfigService;

    @PostMapping("/list")
    public Result<PageResult<NotificationChannelConfig>> list(@RequestBody(required = false) Map<String, Object> params) {
        return Result.ok(channelConfigService.list(params));
    }

    @PostMapping("/get/{id}")
    public Result<NotificationChannelConfig> getById(@PathVariable Long id) {
        return Result.ok(channelConfigService.getById(id));
    }

    @PostMapping("/add")
    @OperateLog(module = "通知渠道配置", action = "add", description = "新增渠道配置")
    public Result<Void> add(@RequestBody NotificationChannelConfig entity) {
        channelConfigService.add(entity);
        return Result.ok();
    }

    @PostMapping("/set")
    @OperateLog(module = "通知渠道配置", action = "update", description = "更新渠道配置")
    public Result<Void> set(@RequestBody NotificationChannelConfig entity) {
        channelConfigService.update(entity);
        return Result.ok();
    }

    @PostMapping("/del/{id}")
    @OperateLog(module = "通知渠道配置", action = "delete", description = "删除渠道配置")
    public Result<Void> del(@PathVariable Long id) {
        channelConfigService.delete(id);
        return Result.ok();
    }
}
