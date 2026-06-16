package com.powersmart.system.service;

import cn.hutool.core.util.StrUtil;
import com.powersmart.system.entity.NotificationDelivery;
import com.powersmart.system.entity.NotificationSubscription;
import com.powersmart.system.entity.NotificationTemplate;
import com.powersmart.system.service.channel.ChannelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 通知分发引擎 — 核心调度服务
 *
 * <p>根据模板、订阅和渠道配置，将通知分发给目标用户。
 * 支持多渠道并发推送，自动记录投递状态。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationDispatchService {

    private final NotificationTemplateService templateService;
    private final NotificationSubscriptionService subscriptionService;
    private final NotificationDeliveryService deliveryService;

    /** Spring 自动注入所有 ChannelService 实现，key 为 bean 名称 */
    private final Map<String, ChannelService> channelMap;

    /**
     * 分发通知给用户
     *
     * @param userId    目标用户 ID
     * @param bizType   业务类型（hazard_approval/device_alarm/work_order/ai_detection）
     * @param level     严重级别（info/warning/critical）
     * @param variables 模板变量
     * @param bizId     可选业务记录 ID
     */
    public void dispatch(Long userId, String bizType, String level,
                         Map<String, Object> variables, Long bizId) {
        // 1. 查找匹配的业务模板
        List<NotificationTemplate> templates = templateService.getByBizType(bizType);
        if (templates.isEmpty()) {
            log.warn("未找到业务类型模板, bizType={}", bizType);
            return;
        }

        // 2. 获取用户订阅
        NotificationSubscription sub = subscriptionService.getByUserAndBizType(userId, bizType);
        List<String> enabledChannels = getEnabledChannels(sub, level);
        if (enabledChannels.isEmpty()) {
            log.debug("用户无可用的通知渠道, userId={}, bizType={}", userId, bizType);
            return;
        }

        // 3. 注入 bizId 到变量
        if (bizId != null && variables != null) {
            variables.put("bizId", bizId);
        }

        // 4. 遍历模板，渲染并分发
        for (NotificationTemplate template : templates) {
            String title = render(template.getTitleTemplate(), variables);
            String content = render(template.getContentTemplate(), variables);

            // 计算目标渠道（模板允许的渠道 ∩ 用户订阅的渠道）
            List<String> targetChannels = getTargetChannels(template.getChannel(), enabledChannels);
            if (targetChannels.isEmpty()) {
                log.debug("模板渠道与用户订阅无交集, templateId={}, userId={}", template.getId(), userId);
                continue;
            }

            for (String channel : targetChannels) {
                // 通过 bean 名称查找渠道服务（驼峰转换）
                ChannelService channelService = resolveChannelService(channel);
                if (channelService == null) {
                    log.warn("未找到渠道服务实现, channel={}", channel);
                    continue;
                }

                // 构建附加参数
                Map<String, Object> params = new LinkedHashMap<>();
                if (variables != null) {
                    params.putAll(variables);
                }
                params.put("bizType", bizType);
                params.put("level", level);
                if (bizId != null) {
                    params.put("bizId", bizId);
                }

                // 执行推送
                String status = "pending";
                String errorMsg = null;
                try {
                    boolean ok = channelService.send(userId, title, content, params);
                    status = ok ? "sent" : "failed";
                    if (!ok) {
                        errorMsg = "渠道返回发送失败";
                    }
                } catch (Exception e) {
                    status = "failed";
                    errorMsg = e.getMessage();
                    log.error("渠道 {} 发送通知失败, userId={}", channel, userId, e);
                }

                // 记录投递
                NotificationDelivery delivery = new NotificationDelivery();
                delivery.setUserId(userId);
                delivery.setChannel(channel);
                delivery.setStatus(status);
                if ("sent".equals(status)) {
                    delivery.setSentAt(LocalDateTime.now());
                }
                delivery.setErrorMsg(errorMsg);
                delivery.setRetryCount(0);
                deliveryService.add(delivery);

                log.info("通知分发完成: userId={}, bizType={}, channel={}, status={}",
                        userId, bizType, channel, status);
            }
        }
    }

    /**
     * 获取用户启用的通知渠道列表
     */
    private List<String> getEnabledChannels(NotificationSubscription sub, String level) {
        if (sub == null || sub.getEnabled() == null || sub.getEnabled() != 1) {
            // 无订阅记录时使用默认渠道
            return getDefaultChannels(level);
        }

        // 检查级别是否满足最低要求
        if (!meetsMinLevel(level, sub.getMinLevel())) {
            log.debug("通知级别 {} 低于用户订阅的最低级别 {}", level, sub.getMinLevel());
            return Collections.emptyList();
        }

        // 解析渠道列表：先尝试 JSON 数组，失败后按逗号分隔
        List<String> channels = parseJsonArray(sub.getChannels());
        if (!channels.isEmpty()) {
            return channels;
        }
        if (StrUtil.isNotBlank(sub.getChannels())) {
            return Arrays.stream(sub.getChannels().split(","))
                    .map(String::trim)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toList());
        }
        return getDefaultChannels(level);
    }

    /**
     * 默认渠道：info/warning → ["in_app"]，critical → ["in_app", "sms"]
     */
    private List<String> getDefaultChannels(String level) {
        if ("critical".equalsIgnoreCase(level)) {
            return new ArrayList<>(Arrays.asList("in_app", "sms"));
        }
        return new ArrayList<>(Collections.singletonList("in_app"));
    }

    /**
     * 检查消息级别是否达到订阅的最低要求
     */
    private boolean meetsMinLevel(String level, String minLevel) {
        if (StrUtil.isBlank(minLevel)) {
            return true;
        }
        int levelPriority = getLevelPriority(level);
        int minPriority = getLevelPriority(minLevel);
        return levelPriority >= minPriority;
    }

    private int getLevelPriority(String level) {
        if (level == null) return 0;
        switch (level.toLowerCase()) {
            case "critical": return 2;
            case "warning":  return 1;
            case "info":     return 0;
            default:         return 0;
        }
    }

    /**
     * 渲染模板 — 将 {var} 占位符替换为实际值
     */
    private String render(String template, Map<String, Object> variables) {
        if (StrUtil.isBlank(template) || variables == null || variables.isEmpty()) {
            return template;
        }
        String result = template;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (key != null && value != null) {
                result = result.replace("{" + key + "}", value.toString());
            }
        }
        return result;
    }

    /**
     * 计算目标渠道：模板允许的渠道与用户订阅的交集
     */
    private List<String> getTargetChannels(String templateChannel, List<String> subChannels) {
        if (StrUtil.isBlank(templateChannel)) {
            return subChannels;
        }
        Set<String> templateChannels = Arrays.stream(templateChannel.split(","))
                .map(String::trim)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());

        if (templateChannels.isEmpty()) {
            return subChannels;
        }

        return subChannels.stream()
                .filter(templateChannels::contains)
                .collect(Collectors.toList());
    }

    /**
     * 根据渠道名称解析对应的 ChannelService bean
     *
     * <p>渠道名转 bean 名规则：
     * <ul>
     *   <li>"in_app" → "inAppChannelService"</li>
     *   <li>"sms" → "smsChannelService"</li>
     *   <li>"email" → "emailChannelService"</li>
     *   <li>"feishu" → "feishuChannelService"</li>
     * </ul>
     */
    private ChannelService resolveChannelService(String channel) {
        if (StrUtil.isBlank(channel)) {
            return null;
        }
        String beanName = toCamelCase(channel) + "ChannelService";
        return channelMap.get(beanName);
    }

    /**
     * 将下划线命名转为驼峰命名
     */
    private String toCamelCase(String name) {
        StringBuilder sb = new StringBuilder();
        boolean nextUpper = false;
        for (char c : name.toCharArray()) {
            if (c == '_') {
                nextUpper = true;
            } else if (nextUpper) {
                sb.append(Character.toUpperCase(c));
                nextUpper = false;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 解析 JSON 数组字符串
     */
    private List<String> parseJsonArray(String json) {
        if (StrUtil.isBlank(json)) {
            return Collections.emptyList();
        }
        try {
            // 简单解析：去掉 [ ] 和引号，按逗号分割
            String trimmed = json.trim();
            if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                trimmed = trimmed.substring(1, trimmed.length() - 1);
            }
            return Arrays.stream(trimmed.split(","))
                    .map(s -> s.trim().replace("\"", ""))
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("解析渠道 JSON 数组失败: {}", json, e);
            return Collections.emptyList();
        }
    }
}
