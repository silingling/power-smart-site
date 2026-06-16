package com.powersmart.system.controller;

import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.system.entity.NotificationDelivery;
import com.powersmart.system.service.NotificationDeliveryService;
import com.powersmart.system.service.NotificationDispatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/build/notificationDispatch")
public class NotificationDispatchController {

    private final NotificationDispatchService dispatchService;
    private final NotificationDeliveryService deliveryService;

    @PostMapping("/dispatch")
    public Result<Void> dispatch(@RequestBody Map<String, Object> params) {
        Long userId = params.get("userId") != null ? Long.valueOf(params.get("userId").toString()) : null;
        String bizType = (String) params.get("bizType");
        String level = (String) params.get("level");
        @SuppressWarnings("unchecked")
        Map<String, Object> variables = (Map<String, Object>) params.get("variables");
        Long bizId = params.get("bizId") != null ? Long.valueOf(params.get("bizId").toString()) : null;
        if (userId == null || bizType == null) {
            return Result.fail("userId 和 bizType 不能为空");
        }
        dispatchService.dispatch(userId, bizType, level != null ? level : "info", variables, bizId);
        return Result.ok();
    }

    @PostMapping("/getDeliveryList")
    public Result<PageResult<NotificationDelivery>> getDeliveryList(@RequestBody(required = false) Map<String, Object> params) {
        return Result.ok(deliveryService.list(params));
    }

    @PostMapping("/getDelivery/{id}")
    public Result<NotificationDelivery> getDelivery(@PathVariable Long id) {
        return Result.ok(deliveryService.getById(id));
    }
}
