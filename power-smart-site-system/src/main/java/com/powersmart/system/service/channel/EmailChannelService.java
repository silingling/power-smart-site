package com.powersmart.system.service.channel;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 邮件通知渠道（桩实现）
 */
@Slf4j
@Service("emailChannelService")
public class EmailChannelService implements ChannelService {

    @Override
    public String getChannel() {
        return "email";
    }

    @Override
    public boolean send(Long userId, String title, String content, Map<String, Object> params) {
        log.info("[邮件通知] userId={}, title={}, content={}", userId, title, content);
        // TODO: 对接实际邮件发送服务
        return true;
    }
}
