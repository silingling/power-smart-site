package com.powersmart.device.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.powersmart.common.push.SsePushService;
import com.powersmart.device.entity.SystemNotification;
import com.powersmart.device.mapper.SystemNotificationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统通知服务
 *
 * <p>创建、查询、推送站内通知。
 * 所有模块均可通过 Feign 客户端或直接 DB 操作调用。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SystemNotificationMapper notificationMapper;
    private final SsePushService ssePushService;
    private final ObjectMapper objectMapper;

    /**
     * 发送通知给单个用户（含 SSE 实时推送）
     */
    public SystemNotification send(Long userId, String title, String content,
                                    String bizType, Long bizId, String level) {
        SystemNotification notif = new SystemNotification();
        notif.setUserId(userId);
        notif.setTitle(title);
        notif.setContent(content);
        notif.setBizType(bizType);
        notif.setBizId(bizId);
        notif.setLevel(level != null ? level : "info");
        notif.setIsRead(0);
        notificationMapper.insert(notif);

        // SSE 推送
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("type", "notification");
            payload.put("notificationId", notif.getId());
            payload.put("title", title);
            payload.put("content", content);
            payload.put("bizType", bizType);
            payload.put("bizId", bizId);
            payload.put("level", level);
            payload.put("timestamp", System.currentTimeMillis());

            String json = objectMapper.writeValueAsString(payload);
            ssePushService.push(userId, "notification", json);
        } catch (Exception e) {
            log.warn("SSE 推送通知失败", e);
        }

        return notif;
    }

    /**
     * 查询用户通知列表（分页）
     */
    public Page<SystemNotification> getUserNotifications(Long userId, int page, int size, Boolean unreadOnly) {
        Page<SystemNotification> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<SystemNotification> wrapper = new LambdaQueryWrapper<SystemNotification>()
                .eq(SystemNotification::getUserId, userId)
                .orderByDesc(SystemNotification::getCreatedAt);

        if (Boolean.TRUE.equals(unreadOnly)) {
            wrapper.eq(SystemNotification::getIsRead, 0);
        }

        return notificationMapper.selectPage(pageParam, wrapper);
    }

    /**
     * 获取未读通知数
     */
    public long getUnreadCount(Long userId) {
        return notificationMapper.selectCount(new LambdaQueryWrapper<SystemNotification>()
                .eq(SystemNotification::getUserId, userId)
                .eq(SystemNotification::getIsRead, 0));
    }

    /**
     * 标记通知为已读
     */
    public void markAsRead(Long notificationId) {
        SystemNotification notif = notificationMapper.selectById(notificationId);
        if (notif != null) {
            notif.setIsRead(1);
            notif.setReadAt(LocalDateTime.now());
            notificationMapper.updateById(notif);
        }
    }

    /**
     * 标记用户所有通知为已读
     */
    public void markAllAsRead(Long userId) {
        notificationMapper.selectList(
                new LambdaQueryWrapper<SystemNotification>()
                        .eq(SystemNotification::getUserId, userId)
                        .eq(SystemNotification::getIsRead, 0)
        ).forEach(n -> {
            n.setIsRead(1);
            n.setReadAt(LocalDateTime.now());
            notificationMapper.updateById(n);
        });
    }

    /**
     * 删除通知
     */
    public void deleteNotification(Long notificationId) {
        notificationMapper.deleteById(notificationId);
    }
}
