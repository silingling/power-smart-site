package com.powersmart.system.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.annotation.OperateLog;
import com.powersmart.common.util.PageHelper;
import com.powersmart.system.entity.NotificationSubscription;
import com.powersmart.system.mapper.NotificationSubscriptionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 通知订阅 CRUD 服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
@OperateLog(module = "通知订阅")
public class NotificationSubscriptionService {

    private final NotificationSubscriptionMapper mapper;

    /**
     * 分页查询订阅列表
     */
    public Page<NotificationSubscription> list(Map<String, Object> params) {
        Page<NotificationSubscription> pageParam = PageHelper.of(params);
        LambdaQueryWrapper<NotificationSubscription> wrapper = new LambdaQueryWrapper<>();

        if (params != null) {
            String userId = getParamStr(params, "userId");
            String bizType = getParamStr(params, "bizType");
            String enabled = getParamStr(params, "enabled");

            if (StrUtil.isNotBlank(userId)) {
                wrapper.eq(NotificationSubscription::getUserId, Long.parseLong(userId));
            }
            if (StrUtil.isNotBlank(bizType)) {
                wrapper.eq(NotificationSubscription::getBizType, bizType);
            }
            if (StrUtil.isNotBlank(enabled)) {
                wrapper.eq(NotificationSubscription::getEnabled, Integer.parseInt(enabled));
            }
        }

        wrapper.orderByDesc(NotificationSubscription::getId);
        return mapper.selectPage(pageParam, wrapper);
    }

    /**
     * 根据 ID 获取订阅
     */
    public NotificationSubscription getById(Long id) {
        return mapper.selectById(id);
    }

    /**
     * 新增订阅
     */
    public void add(NotificationSubscription entity) {
        mapper.insert(entity);
    }

    /**
     * 更新订阅
     */
    public void update(NotificationSubscription entity) {
        mapper.updateById(entity);
    }

    /**
     * 删除订阅
     */
    public void delete(Long id) {
        mapper.deleteById(id);
    }

    /**
     * 获取用户的所有订阅
     */
    public List<NotificationSubscription> getByUser(Long userId) {
        return mapper.selectList(new LambdaQueryWrapper<NotificationSubscription>()
                .eq(NotificationSubscription::getUserId, userId)
                .eq(NotificationSubscription::getEnabled, 1));
    }

    /**
     * 获取用户对某业务类型的订阅
     */
    public NotificationSubscription getByUserAndBizType(Long userId, String bizType) {
        return mapper.selectOne(new LambdaQueryWrapper<NotificationSubscription>()
                .eq(NotificationSubscription::getUserId, userId)
                .eq(NotificationSubscription::getBizType, bizType)
                .eq(NotificationSubscription::getEnabled, 1));
    }

    private String getParamStr(Map<String, Object> params, String key) {
        if (params == null || !params.containsKey(key)) return null;
        Object val = params.get(key);
        return val != null ? val.toString() : null;
    }
}
