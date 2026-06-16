package com.powersmart.hazard.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powersmart.common.constant.ApiConstant;
import com.powersmart.common.exception.BusinessException;
import com.powersmart.hazard.entity.HazardReport;
import com.powersmart.hazard.entity.HazardWorkOrder;
import com.powersmart.hazard.mapper.HazardReportMapper;
import com.powersmart.hazard.mapper.HazardWorkOrderMapper;
import com.powersmart.hazard.service.ApprovalService;
import com.powersmart.hazard.service.HazardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HazardServiceImpl extends ServiceImpl<HazardReportMapper, HazardReport> implements HazardService {

    private final HazardWorkOrderMapper workOrderMapper;
    private final ApprovalService approvalService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HazardReport reportHazard(HazardReport report, Long assigneeId) {
        save(report);

        // 启动审批流程（≥ 较大隐患）
        approvalService.startApproval(report.getId());

        // 重大/特别重大隐患自动派单到指定责任人
        if (report.getHazardLevel() != null && report.getHazardLevel() >= ApiConstant.HazardLevel.MAJOR) {
            createWorkOrder(report.getId(), assigneeId, null, 4);
        }
        return report;
    }

    @Override
    public HazardWorkOrder createWorkOrder(Long hazardId, Long assigneeId, Long teamId, int deadlineHours) {
        HazardWorkOrder order = new HazardWorkOrder();
        order.setHazardId(hazardId);
        order.setAssigneeId(assigneeId);
        order.setAssigneeTeamId(teamId);
        order.setDeadline(LocalDateTime.now().plusHours(deadlineHours));
        order.setStatus(ApiConstant.WorkOrderStatus.PENDING);
        order.setEscalated(false);
        workOrderMapper.insert(order);
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HazardWorkOrder submitRectification(Long orderId, String note, String images) {
        HazardWorkOrder order = workOrderMapper.selectById(orderId);
        if (order == null) throw new BusinessException("工单不存在");
        order.setRectificationNote(note);
        order.setRectificationImages(images);
        order.setRectificationTime(LocalDateTime.now());
        order.setStatus(ApiConstant.WorkOrderStatus.RECTIFIED);
        workOrderMapper.updateById(order);
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HazardWorkOrder verifyWorkOrder(Long orderId, Long verifierId, boolean passed, String note) {
        HazardWorkOrder order = workOrderMapper.selectById(orderId);
        if (order == null) throw new BusinessException("工单不存在");
        order.setVerifiedBy(verifierId);
        order.setVerifiedNote(note);
        order.setVerifiedTime(LocalDateTime.now());
        order.setStatus(passed ? ApiConstant.WorkOrderStatus.PASSED : ApiConstant.WorkOrderStatus.REJECTED);
        workOrderMapper.updateById(order);

        // 更新隐患状态
        if (passed) {
            HazardReport report = getById(order.getHazardId());
            if (report != null) {
                report.setStatus(ApiConstant.HazardStatus.VERIFIED);
                updateById(report);
            }
        }
        return order;
    }

    @Override
    public Object getHazardProgress(Long hazardId) {
        HazardReport report = getById(hazardId);
        if (report == null) return null;

        List<HazardWorkOrder> orders = workOrderMapper.selectList(
                new LambdaQueryWrapper<HazardWorkOrder>()
                        .eq(HazardWorkOrder::getHazardId, hazardId));

        java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("hazard", report);
        result.put("workOrders", orders);
        result.put("currentNode", approvalService.getCurrentNodeName(hazardId));
        result.put("approvalHistory", approvalService.getApprovalHistory(hazardId));
        return result;
    }

    @Override
    public Page<HazardWorkOrder> queryWorkOrderPage(Page<HazardWorkOrder> page, java.util.Map<String, Object> params) {
        LambdaQueryWrapper<HazardWorkOrder> wrapper = new LambdaQueryWrapper<HazardWorkOrder>()
                .orderByDesc(HazardWorkOrder::getCreatedAt);
        if (params != null) {
            if (params.containsKey("status") && params.get("status") != null)
                wrapper.eq(HazardWorkOrder::getStatus, Integer.parseInt(params.get("status").toString()));
            if (params.containsKey("hazardId") && params.get("hazardId") != null)
                wrapper.eq(HazardWorkOrder::getHazardId, Long.parseLong(params.get("hazardId").toString()));
        }
        return workOrderMapper.selectPage(page, wrapper);
    }

    @Override
    public Object getHazardStats(Long projectId) {
        long total = count(new LambdaQueryWrapper<HazardReport>()
                .eq(HazardReport::getProjectId, projectId));
        long pending = count(new LambdaQueryWrapper<HazardReport>()
                .eq(HazardReport::getProjectId, projectId).eq(HazardReport::getStatus, 1));
        long verified = count(new LambdaQueryWrapper<HazardReport>()
                .eq(HazardReport::getProjectId, projectId).eq(HazardReport::getStatus, 3));
        long major = count(new LambdaQueryWrapper<HazardReport>()
                .eq(HazardReport::getProjectId, projectId).ge(HazardReport::getHazardLevel, 2));

        java.util.Map<String, Object> stats = new java.util.LinkedHashMap<>();
        stats.put("total", total);
        stats.put("pending", pending);
        stats.put("verified", verified);
        stats.put("major", major);
        return stats;
    }

    @Override
    public Page<HazardReport> page(Page<HazardReport> page, LambdaQueryWrapper<HazardReport> wrapper) {
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public List<HazardReport> getByProject(Long projectId, Integer status, String hazardType, Integer level) {
        return list(new LambdaQueryWrapper<HazardReport>()
                .eq(HazardReport::getProjectId, projectId)
                .eq(status != null && status > 0, HazardReport::getStatus, status)
                .eq(hazardType != null && !hazardType.isEmpty(), HazardReport::getHazardType, hazardType)
                .eq(level != null && level > 0, HazardReport::getHazardLevel, level)
                .orderByDesc(HazardReport::getCreatedAt));
    }

    @Override
    public void updateWorkOrder(HazardWorkOrder order) {
        workOrderMapper.updateById(order);
    }

    @Override
    public List<HazardWorkOrder> getOverdueOrders() {
        List<HazardWorkOrder> overdue = workOrderMapper.selectList(new LambdaQueryWrapper<HazardWorkOrder>()
                .eq(HazardWorkOrder::getStatus, ApiConstant.WorkOrderStatus.PENDING)
                .lt(HazardWorkOrder::getDeadline, LocalDateTime.now()));

        // 标记超时工单
        if (CollUtil.isNotEmpty(overdue)) {
            for (HazardWorkOrder order : overdue) {
                if (!order.getEscalated()) {
                    order.setEscalated(true);
                    workOrderMapper.updateById(order);
                }
            }
        }

        return overdue;
    }
}
