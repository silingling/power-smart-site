package com.powersmart.system.controller;

import com.powersmart.common.annotation.OperateLog;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.system.entity.NotificationSubscription;
import com.powersmart.system.service.NotificationSubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/build/notificationSubscription")
public class NotificationSubscriptionController {

    private final NotificationSubscriptionService subscriptionService;

    @PostMapping("/list")
    public Result<PageResult<NotificationSubscription>> list(@RequestBody(required = false) Map<String, Object> params) {
        return Result.ok(subscriptionService.list(params));
    }

    @PostMapping("/get/{id}")
    public Result<NotificationSubscription> getById(@PathVariable Long id) {
        return Result.ok(subscriptionService.getById(id));
    }

    @PostMapping("/add")
    @OperateLog(module = "通知订阅", action = "add", description = "新增订阅")
    public Result<Void> add(@RequestBody NotificationSubscription entity) {
        subscriptionService.add(entity);
        return Result.ok();
    }

    @PostMapping("/set")
    @OperateLog(module = "通知订阅", action = "update", description = "更新订阅")
    public Result<Void> set(@RequestBody NotificationSubscription entity) {
        subscriptionService.update(entity);
        return Result.ok();
    }

    @PostMapping("/del/{id}")
    @OperateLog(module = "通知订阅", action = "delete", description = "删除订阅")
    public Result<Void> del(@PathVariable Long id) {
        subscriptionService.delete(id);
        return Result.ok();
    }

    @PostMapping("/getMySubscriptions")
    public Result<Object> getMySubscriptions(@RequestBody(required = false) Map<String, Object> params) {
        return Result.ok(subscriptionService.list(params));
    }
}
