package com.powersmart.hazard.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.powersmart.hazard.entity.HazardReport;
import com.powersmart.hazard.entity.HazardWorkOrder;
import java.util.List;

public interface HazardService extends IService<HazardReport> {

    /**
     * 更新工单
     */
    void updateWorkOrder(HazardWorkOrder order);

    /**
     * 上报隐患并自动派发工单
     */
    HazardReport reportHazard(HazardReport report, Long assigneeId);

    /**
     * 创建整改工单
     */
    HazardWorkOrder createWorkOrder(Long hazardId, Long assigneeId, Long teamId, int deadlineHours);

    /**
     * 提交整改
     */
    HazardWorkOrder submitRectification(Long orderId, String note, String images);

    /**
     * 验收工单
     */
    HazardWorkOrder verifyWorkOrder(Long orderId, Long verifierId, boolean passed, String note);

    /**
     * 查询超期未整改工单
     */
    List<HazardWorkOrder> getOverdueOrders();

    /**
     * 分页查询整改工单
     */
    com.baomidou.mybatisplus.extension.plugins.pagination.Page<HazardWorkOrder> queryWorkOrderPage(
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<HazardWorkOrder> page,
            java.util.Map<String, Object> params);

    /**
     * 查询隐患的完整进展（含工单、审批、文件）
     */
    Object getHazardProgress(Long hazardId);

    /**
     * 分页查询隐患（给 HazardBuildController 使用）
     */
    com.baomidou.mybatisplus.extension.plugins.pagination.Page<HazardReport> page(
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<HazardReport> page,
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<HazardReport> wrapper);

    /**
     * 获取隐患统计
     */
    Object getHazardStats(Long projectId);

    /**
     * 按项目批量查询隐患列表
     */
    List<HazardReport> getByProject(Long projectId, Integer status, String hazardType, Integer level);
}
