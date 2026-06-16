package com.powersmart.system.service.channel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.powersmart.common.push.SsePushService;
import com.powersmart.device.entity.SystemNotification;
import com.powersmart.device.mapper.SystemNotificationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 站内通知渠道 — 创建 SystemNotification 记录并通过 SSE 实时推送
 */
@Slf4j
@Service("inAppChannelService")
@RequiredArgsConstructor
public class InAppChannelService implements ChannelService {

    private final SystemNotificationMapper notificationMapper;
    private final SsePushService ssePushService;
    private final ObjectMapper objectMapper;

    @Override
    public String getChannel() {
        return "in_app";
    }

    @Override
    public boolean send(Long userId, String title, String content, Map<String, Object> params) {
        // 创建系统通知记录
        SystemNotification notif = new SystemNotification();
        notif.setUserId(userId);
        notif.setTitle(title);
        notif.setContent(content);
        notif.setBizType(params != null ? (String) params.get("bizType") : null);
        notif.setBizId(params != null && params.get("bizId") != null
                ? Long.valueOf(params.get("bizId").toString()) : null);
        notif.setLevel(params != null ? (String) params.get("level") : "info");
        notif.setIsRead(0);
        notificationMapper.insert(notif);

        // SSE 实时推送
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("type", "notification");
            payload.put("notificationId", notif.getId());
            payload.put("title", title);
            payload.put("content", content);
            payload.put("bizType", notif.getBizType());
            payload.put("bizId", notif.getBizId());
            payload.put("level", notif.getLevel());
            payload.put("timestamp", System.currentTimeMillis());

            String json = objectMapper.writeValueAsString(payload);
            ssePushService.push(userId, "notification", json);
        } catch (Exception e) {
            log.warn("SSE 推送通知失败, userId={}", userId, e);
        }

        return true;
    }
}
