package com.powersmart.system.service.channel;

import java.util.Map;

/**
 * 通知渠道服务接口 — 所有通知渠道需实现此接口
 */
public interface ChannelService {

    /**
     * 获取渠道名称
     */
    String getChannel();

    /**
     * 发送通知
     *
     * @param userId  目标用户 ID
     * @param title   通知标题
     * @param content 通知内容
     * @param params  附加参数（可包含 bizType/bizId/level 等）
     * @return 是否发送成功
     */
    boolean send(Long userId, String title, String content, Map<String, Object> params);
}
