package com.powersmart.system.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.annotation.OperateLog;
import com.powersmart.common.util.PageHelper;
import com.powersmart.system.entity.NotificationDelivery;
import com.powersmart.system.mapper.NotificationDeliveryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 通知投递记录 CRUD 服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
@OperateLog(module = "通知投递")
public class NotificationDeliveryService {

    private final NotificationDeliveryMapper mapper;

    /**
     * 分页查询投递记录
     */
    public Page<NotificationDelivery> list(Map<String, Object> params) {
        Page<NotificationDelivery> pageParam = PageHelper.of(params);
        LambdaQueryWrapper<NotificationDelivery> wrapper = new LambdaQueryWrapper<>();

        if (params != null) {
            String userId = getParamStr(params, "userId");
            String notificationId = getParamStr(params, "notificationId");
            String channel = getParamStr(params, "channel");
            String status = getParamStr(params, "status");

            if (StrUtil.isNotBlank(userId)) {
                wrapper.eq(NotificationDelivery::getUserId, Long.parseLong(userId));
            }
            if (StrUtil.isNotBlank(notificationId)) {
                wrapper.eq(NotificationDelivery::getNotificationId, Long.parseLong(notificationId));
            }
            if (StrUtil.isNotBlank(channel)) {
                wrapper.eq(NotificationDelivery::getChannel, channel);
            }
            if (StrUtil.isNotBlank(status)) {
                wrapper.eq(NotificationDelivery::getStatus, status);
            }
        }

        wrapper.orderByDesc(NotificationDelivery::getId);
        return mapper.selectPage(pageParam, wrapper);
    }

    /**
     * 根据 ID 获取投递记录
     */
    public NotificationDelivery getById(Long id) {
        return mapper.selectById(id);
    }

    /**
     * 新增投递记录
     */
    public void add(NotificationDelivery entity) {
        mapper.insert(entity);
    }

    /**
     * 更新投递状态
     */
    public void updateStatus(Long id, String status, String errorMsg) {
        NotificationDelivery delivery = mapper.selectById(id);
        if (delivery != null) {
            delivery.setStatus(status);
            delivery.setErrorMsg(errorMsg);
            mapper.updateById(delivery);
        }
    }

    /**
     * 获取某通知的所有投递记录
     */
    public List<NotificationDelivery> getByNotification(Long notificationId) {
        return mapper.selectList(new LambdaQueryWrapper<NotificationDelivery>()
                .eq(NotificationDelivery::getNotificationId, notificationId)
                .orderByDesc(NotificationDelivery::getId));
    }

    private String getParamStr(Map<String, Object> params, String key) {
        if (params == null || !params.containsKey(key)) return null;
        Object val = params.get(key);
        return val != null ? val.toString() : null;
    }
}
