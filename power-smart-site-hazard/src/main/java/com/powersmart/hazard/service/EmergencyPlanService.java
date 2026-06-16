package com.powersmart.hazard.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.annotation.OperateLog;
import com.powersmart.common.auth.SecurityContext;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.util.PageHelper;
import com.powersmart.hazard.entity.EmergencyPlan;
import com.powersmart.hazard.mapper.EmergencyPlanMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@OperateLog
public class EmergencyPlanService {

    private final EmergencyPlanMapper emergencyPlanMapper;

    public PageResult<EmergencyPlan> list(Map<String, Object> params) {
        Page<EmergencyPlan> page = PageHelper.of(params);
        LambdaQueryWrapper<EmergencyPlan> wrapper = new LambdaQueryWrapper<>();
        if (params != null) {
            if (params.containsKey("projectId") && params.get("projectId") != null)
                wrapper.eq(EmergencyPlan::getProjectId, Long.valueOf(params.get("projectId").toString()));
            if (params.containsKey("planType") && params.get("planType") != null)
                wrapper.eq(EmergencyPlan::getPlanType, params.get("planType").toString());
            if (params.containsKey("status") && params.get("status") != null)
                wrapper.eq(EmergencyPlan::getStatus, params.get("status").toString());
            if (params.containsKey("keyword") && StrUtil.isNotBlank(params.get("keyword").toString()))
                wrapper.like(EmergencyPlan::getPlanName, params.get("keyword").toString());
        }
        wrapper.orderByDesc(EmergencyPlan::getCreatedAt);
        return PageResult.from(emergencyPlanMapper.selectPage(page, wrapper));
    }

    public EmergencyPlan getById(Long id) {
        return emergencyPlanMapper.selectById(id);
    }

    public void add(EmergencyPlan entity) {
        if (StrUtil.isBlank(entity.getPlanName())) {
            throw new IllegalArgumentException("预案名称不能为空");
        }
        if (StrUtil.isBlank(entity.getPlanType())) {
            throw new IllegalArgumentException("预案类型不能为空");
        }
        entity.setCreatedBy(SecurityContext.getCurrentUserId());
        emergencyPlanMapper.insert(entity);
    }

    public void update(EmergencyPlan entity) {
        EmergencyPlan existing = emergencyPlanMapper.selectById(entity.getId());
        if (existing == null) {
            throw new IllegalArgumentException("预案不存在");
        }
        if (StrUtil.isNotBlank(entity.getPlanName())) {
            existing.setPlanName(entity.getPlanName());
        }
        if (StrUtil.isNotBlank(entity.getPlanType())) {
            existing.setPlanType(entity.getPlanType());
        }
        if (StrUtil.isNotBlank(entity.getEmergencyLevel())) {
            existing.setEmergencyLevel(entity.getEmergencyLevel());
        }
        if (entity.getDescription() != null) {
            existing.setDescription(entity.getDescription());
        }
        if (entity.getProcedures() != null) {
            existing.setProcedures(entity.getProcedures());
        }
        if (StrUtil.isNotBlank(entity.getResponsiblePerson())) {
            existing.setResponsiblePerson(entity.getResponsiblePerson());
        }
        if (StrUtil.isNotBlank(entity.getResponsiblePhone())) {
            existing.setResponsiblePhone(entity.getResponsiblePhone());
        }
        if (StrUtil.isNotBlank(entity.getResponsibleDept())) {
            existing.setResponsibleDept(entity.getResponsibleDept());
        }
        if (entity.getDrillRequired() != null) {
            existing.setDrillRequired(entity.getDrillRequired());
        }
        if (StrUtil.isNotBlank(entity.getDrillFrequency())) {
            existing.setDrillFrequency(entity.getDrillFrequency());
        }
        if (entity.getAttachmentJson() != null) {
            existing.setAttachmentJson(entity.getAttachmentJson());
        }
        if (StrUtil.isNotBlank(entity.getStatus())) {
            existing.setStatus(entity.getStatus());
        }
        emergencyPlanMapper.updateById(existing);
    }

    public void delete(Long id) {
        emergencyPlanMapper.deleteById(id);
    }

    public List<EmergencyPlan> getActivePlans(Long projectId) {
        return emergencyPlanMapper.selectList(
                new LambdaQueryWrapper<EmergencyPlan>()
                        .eq(EmergencyPlan::getProjectId, projectId)
                        .eq(EmergencyPlan::getStatus, "active")
                        .orderByAsc(EmergencyPlan::getPlanName));
    }
}
