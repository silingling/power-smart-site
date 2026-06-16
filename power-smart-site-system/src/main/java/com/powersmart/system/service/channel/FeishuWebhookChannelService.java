package com.powersmart.system.service.channel;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 飞书 Webhook 通知渠道（桩实现）
 */
@Slf4j
@Service("feishuChannelService")
public class FeishuWebhookChannelService implements ChannelService {

    @Override
    public String getChannel() {
        return "feishu";
    }

    @Override
    public boolean send(Long userId, String title, String content, Map<String, Object> params) {
        log.info("[飞书Webhook通知] userId={}, title={}, content={}", userId, title, content);
        // TODO: 对接飞书 Webhook 发送实际消息
        return true;
    }
}
