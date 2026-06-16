package com.powersmart.hazard.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.annotation.OperateLog;
import com.powersmart.common.util.PageHelper;
import com.powersmart.common.entity.PageResult;
import com.powersmart.hazard.entity.InspectionTemplate;
import com.powersmart.hazard.mapper.InspectionTemplateMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@OperateLog(module = "巡检模板管理")
public class InspectionTemplateService {

    private final InspectionTemplateMapper inspectionTemplateMapper;

    public PageResult<InspectionTemplate> list(Map<String, Object> params) {
        Page<InspectionTemplate> page = PageHelper.of(params);
        LambdaQueryWrapper<InspectionTemplate> wrapper = new LambdaQueryWrapper<>();
        if (params != null) {
            if (params.containsKey("projectId") && params.get("projectId") != null)
                wrapper.eq(InspectionTemplate::getProjectId, Long.valueOf(params.get("projectId").toString()));
            if (params.containsKey("templateType") && params.get("templateType") != null)
                wrapper.eq(InspectionTemplate::getTemplateType, params.get("templateType").toString());
        }
        wrapper.orderByDesc(InspectionTemplate::getCreatedAt);
        return PageResult.from(inspectionTemplateMapper.selectPage(page, wrapper));
    }

    public InspectionTemplate getById(Long id) {
        return inspectionTemplateMapper.selectById(id);
    }

    @OperateLog(description = "新增巡检模板")
    public void add(InspectionTemplate entity) {
        if (StrUtil.isBlank(entity.getTemplateName())) {
            throw new IllegalArgumentException("模板名称不能为空");
        }
        if (StrUtil.isBlank(entity.getCheckItems())) {
            throw new IllegalArgumentException("检查项不能为空");
        }
        // JSON 语法校验
        try {
            com.alibaba.fastjson2.JSON.parseArray(entity.getCheckItems());
        } catch (Exception e) {
            throw new IllegalArgumentException("检查项JSON格式错误");
        }
        if (entity.getEnabled() == null) {
            entity.setEnabled(1);
        }
        inspectionTemplateMapper.insert(entity);
    }

    @OperateLog(description = "修改巡检模板")
    public void update(InspectionTemplate entity) {
        InspectionTemplate existing = inspectionTemplateMapper.selectById(entity.getId());
        if (existing == null) {
            throw new IllegalArgumentException("巡检模板不存在");
        }
        if (StrUtil.isNotBlank(entity.getTemplateName())) existing.setTemplateName(entity.getTemplateName());
        if (StrUtil.isNotBlank(entity.getTemplateType())) existing.setTemplateType(entity.getTemplateType());
        if (entity.getCheckItems() != null) {
            try {
                com.alibaba.fastjson2.JSON.parseArray(entity.getCheckItems());
            } catch (Exception e) {
                throw new IllegalArgumentException("检查项JSON格式错误");
            }
            existing.setCheckItems(entity.getCheckItems());
        }
        if (entity.getEnabled() != null) existing.setEnabled(entity.getEnabled());
        inspectionTemplateMapper.updateById(existing);
    }

    @OperateLog(description = "删除巡检模板")
    public void delete(Long id) {
        inspectionTemplateMapper.deleteById(id);
    }

    public List<InspectionTemplate> getByType(Long projectId, String templateType) {
        LambdaQueryWrapper<InspectionTemplate> wrapper = new LambdaQueryWrapper<InspectionTemplate>()
                .eq(InspectionTemplate::getProjectId, projectId)
                .eq(InspectionTemplate::getEnabled, 1);
        if (StrUtil.isNotBlank(templateType)) {
            wrapper.eq(InspectionTemplate::getTemplateType, templateType);
        }
        wrapper.orderByDesc(InspectionTemplate::getCreatedAt);
        return inspectionTemplateMapper.selectList(wrapper);
    }
}
