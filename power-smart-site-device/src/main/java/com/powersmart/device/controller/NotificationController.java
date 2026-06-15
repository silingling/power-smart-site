package com.powersmart.device.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.auth.SecurityContext;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.common.push.SsePushService;
import com.powersmart.device.entity.SystemNotification;
import com.powersmart.device.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 系统通知 + SSE 实时推送 — /build/notification/* + /api/v1/sse/*
 *
 * 通知列表 / 未读数 / 标记已读 / SSE 订阅
 */
@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final SsePushService ssePushService;

    // ==================== SSE 订阅 ====================

    /** SSE 实时连接订阅 */
    @GetMapping("/api/v1/sse/subscribe")
    public SseEmitter subscribe() {
        Long userId = SecurityContext.getCurrentUserId();
        if (userId == null) userId = 0L;
        return ssePushService.subscribe(userId);
    }

    // ==================== 通知管理 /build/notification ====================

    /** 查询当前用户通知列表 */
    @PostMapping("/build/notification/queryNotificationList")
    public Result<PageResult<Map<String, Object>>> queryList(@RequestBody(required = false) Map<String, Object> params) {
        Long userId = SecurityContext.getCurrentUserId();
        int pageNum = 1, pageSize = 20;
        boolean unreadOnly = false;

        if (params != null) {
            if (params.containsKey("page")) pageNum = Integer.parseInt(params.get("page").toString());
            if (params.containsKey("pageSize")) pageSize = Integer.parseInt(params.get("pageSize").toString());
            if (params.containsKey("unreadOnly")) unreadOnly = Boolean.parseBoolean(params.get("unreadOnly").toString());
        }

        Page<SystemNotification> page = notificationService.getUserNotifications(userId, pageNum, pageSize, unreadOnly);
        List<Map<String, Object>> list = page.getRecords().stream().map(n -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", n.getId());
            m.put("title", n.getTitle());
            m.put("content", n.getContent());
            m.put("bizType", n.getBizType());
            m.put("bizId", n.getBizId());
            m.put("level", n.getLevel());
            m.put("isRead", n.getIsRead());
            m.put("createdAt", n.getCreatedAt() != null ? n.getCreatedAt().toString() : "");
            return m;
        }).collect(Collectors.toList());

        return Result.ok(PageResult.of(list, page.getTotal(), pageNum, pageSize));
    }

    /** 获取未读通知数 */
    @PostMapping("/build/notification/getUnreadCount")
    public Result<Map<String, Object>> getUnreadCount() {
        Long userId = SecurityContext.getCurrentUserId();
        long count = notificationService.getUnreadCount(userId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("unreadCount", count);
        result.put("userId", userId);
        return Result.ok(result);
    }

    /** 标记通知为已读 */
    @PostMapping("/build/notification/markAsRead")
    public Result<Void> markAsRead(@RequestBody Map<String, Object> params) {
        Long id = Long.parseLong(params.getOrDefault("id", 0).toString());
        notificationService.markAsRead(id);
        return Result.ok();
    }

    /** 标记所有通知为已读 */
    @PostMapping("/build/notification/markAllAsRead")
    public Result<Void> markAllAsRead() {
        Long userId = SecurityContext.getCurrentUserId();
        notificationService.markAllAsRead(userId);
        return Result.ok();
    }

    /** 删除通知 */
    @PostMapping("/build/notification/delNotification/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return Result.ok();
    }

    /** SSE 在线状态 */
    @PostMapping("/build/notification/getOnlineStatus")
    public Result<Map<String, Object>> getOnlineStatus() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("onlineCount", ssePushService.getOnlineCount());
        return Result.ok(result);
    }
}
