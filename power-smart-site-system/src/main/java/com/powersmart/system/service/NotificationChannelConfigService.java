package com.powersmart.system.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.annotation.OperateLog;
import com.powersmart.common.util.PageHelper;
import com.powersmart.system.entity.NotificationChannelConfig;
import com.powersmart.system.mapper.NotificationChannelConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 通知渠道配置 CRUD 服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
@OperateLog(module = "通知渠道配置")
public class NotificationChannelConfigService {

    private final NotificationChannelConfigMapper mapper;

    /**
     * 分页查询渠道配置
     */
    public Page<NotificationChannelConfig> list(Map<String, Object> params) {
        Page<NotificationChannelConfig> pageParam = PageHelper.of(params);
        LambdaQueryWrapper<NotificationChannelConfig> wrapper = new LambdaQueryWrapper<>();

        if (params != null) {
            String channel = getParamStr(params, "channel");
            String enabled = getParamStr(params, "enabled");

            if (StrUtil.isNotBlank(channel)) {
                wrapper.eq(NotificationChannelConfig::getChannel, channel);
            }
            if (StrUtil.isNotBlank(enabled)) {
                wrapper.eq(NotificationChannelConfig::getEnabled, Integer.parseInt(enabled));
            }
        }

        wrapper.orderByAsc(NotificationChannelConfig::getChannel, NotificationChannelConfig::getId);
        return mapper.selectPage(pageParam, wrapper);
    }

    /**
     * 根据 ID 获取配置
     */
    public NotificationChannelConfig getById(Long id) {
        return mapper.selectById(id);
    }

    /**
     * 新增配置
     */
    public void add(NotificationChannelConfig entity) {
        mapper.insert(entity);
    }

    /**
     * 更新配置
     */
    public void update(NotificationChannelConfig entity) {
        mapper.updateById(entity);
    }

    /**
     * 删除配置
     */
    public void delete(Long id) {
        mapper.deleteById(id);
    }

    /**
     * 获取某渠道的所有配置
     */
    public List<NotificationChannelConfig> getByChannel(String channel) {
        return mapper.selectList(new LambdaQueryWrapper<NotificationChannelConfig>()
                .eq(NotificationChannelConfig::getChannel, channel)
                .eq(NotificationChannelConfig::getEnabled, 1));
    }

    private String getParamStr(Map<String, Object> params, String key) {
        if (params == null || !params.containsKey(key)) return null;
        Object val = params.get(key);
        return val != null ? val.toString() : null;
    }
}
