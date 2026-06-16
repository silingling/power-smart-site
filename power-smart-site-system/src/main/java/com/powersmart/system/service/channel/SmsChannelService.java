package com.powersmart.system.service.channel;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 短信通知渠道（桩实现）
 */
@Slf4j
@Service("smsChannelService")
public class SmsChannelService implements ChannelService {

    @Override
    public String getChannel() {
        return "sms";
    }

    @Override
    public boolean send(Long userId, String title, String content, Map<String, Object> params) {
        log.info("[短信通知] userId={}, title={}, content={}", userId, title, content);
        // TODO: 对接实际短信网关
        return true;
    }
}
