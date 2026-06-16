package com.powersmart.hazard.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.annotation.OperateLog;
import com.powersmart.common.auth.SecurityContext;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.util.PageHelper;
import com.powersmart.hazard.entity.InspectionPlan;
import com.powersmart.hazard.mapper.InspectionPlanMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@OperateLog(module = "巡检计划管理")
public class InspectionPlanService {

    private final InspectionPlanMapper inspectionPlanMapper;

    public PageResult<InspectionPlan> list(Map<String, Object> params) {
        Page<InspectionPlan> page = PageHelper.of(params);
        LambdaQueryWrapper<InspectionPlan> wrapper = new LambdaQueryWrapper<>();
        if (params != null) {
            if (params.containsKey("projectId") && params.get("projectId") != null)
                wrapper.eq(InspectionPlan::getProjectId, Long.valueOf(params.get("projectId").toString()));
            if (params.containsKey("planType") && params.get("planType") != null)
                wrapper.eq(InspectionPlan::getPlanType, params.get("planType").toString());
            if (params.containsKey("status") && params.get("status") != null)
                wrapper.eq(InspectionPlan::getStatus, params.get("status").toString());
        }
        wrapper.orderByDesc(InspectionPlan::getCreatedAt);
        return PageResult.from(inspectionPlanMapper.selectPage(page, wrapper));
    }

    public InspectionPlan getById(Long id) {
        return inspectionPlanMapper.selectById(id);
    }

    @OperateLog(description = "新增巡检计划")
    public void add(InspectionPlan entity) {
        if (StrUtil.isBlank(entity.getPlanName())) {
            throw new IllegalArgumentException("计划名称不能为空");
        }
        if (StrUtil.isBlank(entity.getPlanType())) {
            throw new IllegalArgumentException("计划类型不能为空");
        }
        if (StrUtil.isBlank(entity.getRouteJson())) {
            throw new IllegalArgumentException("巡检路线不能为空");
        }
        inspectionPlanMapper.insert(entity);
    }

    @OperateLog(description = "修改巡检计划")
    public void update(InspectionPlan entity) {
        InspectionPlan existing = inspectionPlanMapper.selectById(entity.getId());
        if (existing == null) {
            throw new IllegalArgumentException("巡检计划不存在");
        }
        if (StrUtil.isNotBlank(entity.getPlanName())) existing.setPlanName(entity.getPlanName());
        if (StrUtil.isNotBlank(entity.getPlanType())) existing.setPlanType(entity.getPlanType());
        if (StrUtil.isNotBlank(entity.getRouteName())) existing.setRouteName(entity.getRouteName());
        if (entity.getRouteJson() != null) existing.setRouteJson(entity.getRouteJson());
        if (entity.getTotalPoints() != null) existing.setTotalPoints(entity.getTotalPoints());
        if (StrUtil.isNotBlank(entity.getFrequency())) existing.setFrequency(entity.getFrequency());
        if (entity.getStartDate() != null) existing.setStartDate(entity.getStartDate());
        if (entity.getEndDate() != null) existing.setEndDate(entity.getEndDate());
        if (entity.getAssignedTo() != null) existing.setAssignedTo(entity.getAssignedTo());
        if (StrUtil.isNotBlank(entity.getAssigneeName())) existing.setAssigneeName(entity.getAssigneeName());
        if (entity.getDescription() != null) existing.setDescription(entity.getDescription());
        if (StrUtil.isNotBlank(entity.getStatus())) existing.setStatus(entity.getStatus());
        inspectionPlanMapper.updateById(existing);
    }

    @OperateLog(description = "删除巡检计划")
    public void delete(Long id) {
        inspectionPlanMapper.deleteById(id);
    }

    public List<InspectionPlan> getActivePlans(Long projectId) {
        return inspectionPlanMapper.selectList(
                new LambdaQueryWrapper<InspectionPlan>()
                        .eq(InspectionPlan::getProjectId, projectId)
                        .eq(InspectionPlan::getStatus, "active")
                        .orderByDesc(InspectionPlan::getCreatedAt));
    }
}
