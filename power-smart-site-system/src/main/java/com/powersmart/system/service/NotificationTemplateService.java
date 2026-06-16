package com.powersmart.system.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.annotation.OperateLog;
import com.powersmart.common.util.PageHelper;
import com.powersmart.system.entity.NotificationTemplate;
import com.powersmart.system.mapper.NotificationTemplateMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 通知模板 CRUD 服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
@OperateLog(module = "通知模板")
public class NotificationTemplateService {

    private final NotificationTemplateMapper mapper;

    /**
     * 分页查询模板列表
     */
    public Page<NotificationTemplate> list(Map<String, Object> params) {
        Page<NotificationTemplate> pageParam = PageHelper.of(params);
        LambdaQueryWrapper<NotificationTemplate> wrapper = new LambdaQueryWrapper<>();

        if (params != null) {
            String bizType = getParamStr(params, "bizType");
            String channel = getParamStr(params, "channel");
            String templateName = getParamStr(params, "templateName");
            String templateKey = getParamStr(params, "templateKey");
            String enabled = getParamStr(params, "enabled");

            if (StrUtil.isNotBlank(bizType)) {
                wrapper.eq(NotificationTemplate::getBizType, bizType);
            }
            if (StrUtil.isNotBlank(channel)) {
                wrapper.eq(NotificationTemplate::getChannel, channel);
            }
            if (StrUtil.isNotBlank(templateName)) {
                wrapper.like(NotificationTemplate::getTemplateName, templateName);
            }
            if (StrUtil.isNotBlank(templateKey)) {
                wrapper.eq(NotificationTemplate::getTemplateKey, templateKey);
            }
            if (StrUtil.isNotBlank(enabled)) {
                wrapper.eq(NotificationTemplate::getEnabled, Integer.parseInt(enabled));
            }
        }

        wrapper.orderByDesc(NotificationTemplate::getId);
        return mapper.selectPage(pageParam, wrapper);
    }

    /**
     * 根据 ID 获取模板
     */
    public NotificationTemplate getById(Long id) {
        return mapper.selectById(id);
    }

    /**
     * 新增模板
     */
    public void add(NotificationTemplate template) {
        // 校验 templateKey 唯一性
        Long existingCount = mapper.selectCount(new LambdaQueryWrapper<NotificationTemplate>()
                .eq(NotificationTemplate::getTemplateKey, template.getTemplateKey()));
        if (existingCount > 0) {
            throw new RuntimeException("模板标识已存在: " + template.getTemplateKey());
        }
        mapper.insert(template);
    }

    /**
     * 更新模板
     */
    public void update(NotificationTemplate template) {
        mapper.updateById(template);
    }

    /**
     * 删除模板
     */
    public void delete(Long id) {
        mapper.deleteById(id);
    }

    /**
     * 根据业务类型和渠道查询启用的模板
     */
    public List<NotificationTemplate> getByBizType(String bizType) {
        return mapper.selectList(new LambdaQueryWrapper<NotificationTemplate>()
                .eq(NotificationTemplate::getBizType, bizType)
                .eq(NotificationTemplate::getEnabled, 1));
    }

    private String getParamStr(Map<String, Object> params, String key) {
        if (params == null || !params.containsKey(key)) return null;
        Object val = params.get(key);
        return val != null ? val.toString() : null;
    }
}
