package com.powersmart.hazard.service;

import com.powersmart.hazard.entity.ApprovalRecord;

import java.util.List;

public interface ApprovalService {

    /**
     * 启动审批流程（隐患上报后调用）
     */
    void startApproval(Long hazardId);

    /**
     * 处理审批动作
     * @param hazardId      隐患ID
     * @param action        pass/reject
     * @param operatorId    操作人ID
     * @param operatorName  操作人姓名
     * @param comment       审批意见
     */
    ApprovalRecord processApproval(Long hazardId, String action, Long operatorId, String operatorName, String comment);

    /**
     * 查询隐患的审批记录
     */
    List<ApprovalRecord> getApprovalHistory(Long hazardId);

    /**
     * 查询当前待审批节点（用于显示当前进展）
     */
    String getCurrentNodeName(Long hazardId);

    /**
     * 检查并处理超时审批
     */
    int processTimeoutApprovals();
}
