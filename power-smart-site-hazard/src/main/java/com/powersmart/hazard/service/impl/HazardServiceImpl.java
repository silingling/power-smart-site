package com.powersmart.hazard.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powersmart.common.constant.ApiConstant;
import com.powersmart.common.exception.BusinessException;
import com.powersmart.hazard.entity.HazardReport;
import com.powersmart.hazard.entity.HazardWorkOrder;
import com.powersmart.hazard.mapper.HazardReportMapper;
import com.powersmart.hazard.mapper.HazardWorkOrderMapper;
import com.powersmart.hazard.service.HazardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HazardServiceImpl extends ServiceImpl<HazardReportMapper, HazardReport> implements HazardService {

    private final HazardWorkOrderMapper workOrderMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HazardReport reportHazard(HazardReport report, Long assigneeId) {
        save(report);
        // 重大隐患自动派单到指定责任人
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
    public List<HazardWorkOrder> getOverdueOrders() {
        return workOrderMapper.selectList(new LambdaQueryWrapper<HazardWorkOrder>()
                .eq(HazardWorkOrder::getStatus, ApiConstant.WorkOrderStatus.PENDING)
                .lt(HazardWorkOrder::getDeadline, LocalDateTime.now()));
    }
}
