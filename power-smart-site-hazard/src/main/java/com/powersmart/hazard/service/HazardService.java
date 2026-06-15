package com.powersmart.hazard.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.powersmart.hazard.entity.HazardReport;
import com.powersmart.hazard.entity.HazardWorkOrder;
import java.util.List;

public interface HazardService extends IService<HazardReport> {
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
}
