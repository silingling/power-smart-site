package com.powersmart.common.push;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SSE 实时推送服务
 *
 * <p>基于 Server-Sent Events 实现服务端到前端的实时数据推送。
 * 每个用户一个连接，通过 userId 索引。</p>
 *
 * <p>使用方式：</p>
 * <pre>
 *   // 推送给单个用户
 *   ssePushService.push(userId, "alert", payload);
 *
 *   // 推送给所有用户
 *   ssePushService.pushAll("system", "系统维护通知");
 * </pre>
 */
@Slf4j
@Service
public class SsePushService {

    /** userId → SseEmitter */
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    private static final long SSE_TIMEOUT = 30 * 60 * 1000L; // 30 分钟

    /**
     * 创建 SSE 连接（前端调用 GET /api/v1/sse/subscribe）
     */
    public SseEmitter subscribe(Long userId) {
        // 如果已有连接，先关闭旧连接
        SseEmitter old = emitters.remove(userId);
        if (old != null) {
            old.complete();
        }

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        emitters.put(userId, emitter);

        emitter.onCompletion(() -> {
            emitters.remove(userId);
            log.debug("SSE 连接关闭: userId={}", userId);
        });
        emitter.onTimeout(() -> {
            emitters.remove(userId);
            log.debug("SSE 连接超时: userId={}", userId);
        });
        emitter.onError(e -> {
            emitters.remove(userId);
            log.debug("SSE 连接异常: userId={}, err={}", userId, e.getMessage());
        });

        // 发送初始连接确认
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("{\"userId\":" + userId + ",\"timestamp\":" + System.currentTimeMillis() + "}"));
        } catch (IOException e) {
            emitters.remove(userId);
            log.warn("SSE 初始消息发送失败: userId={}", userId);
        }

        log.info("SSE 连接建立: userId={}, 当前连接数={}", userId, emitters.size());
        return emitter;
    }

    /**
     * 推送给指定用户
     *
     * @param userId  目标用户 ID
     * @param event   事件名称（alert / notification / approval / system）
     * @param data    JSON 格式的数据
     */
    public void push(Long userId, String event, String data) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter == null) {
            log.debug("用户未连接 SSE, userId={}", userId);
            return;
        }
        try {
            emitter.send(SseEmitter.event()
                    .name(event)
                    .data(data));
            log.debug("SSE 推送成功: userId={}, event={}", userId, event);
        } catch (IOException e) {
            emitters.remove(userId);
            log.warn("SSE 推送失败，连接已关闭: userId={}", userId);
        }
    }

    /**
     * 推送给所有已连接的用户
     */
    public void pushAll(String event, String data) {
        emitters.forEach((userId, emitter) -> {
            try {
                emitter.send(SseEmitter.event().name(event).data(data));
            } catch (IOException e) {
                emitters.remove(userId);
            }
        });
        log.debug("SSE 广播: event={}, 接收数={}", event, emitters.size());
    }

    /**
     * 获取当前在线用户数
     */
    public int getOnlineCount() {
        return emitters.size();
    }

    /**
     * 检查用户是否在线
     */
    public boolean isOnline(Long userId) {
        return emitters.containsKey(userId);
    }
}
