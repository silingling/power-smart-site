package com.powersmart.hazard.scheduled;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.powersmart.hazard.entity.HazardWorkOrder;
import com.powersmart.hazard.entity.SpecialWorkPermit;
import com.powersmart.hazard.mapper.SpecialWorkPermitMapper;
import com.powersmart.hazard.service.ApprovalService;
import com.powersmart.hazard.service.HazardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务：处理超期工单 + 超时审批 + 到期作业票
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HazardScheduledTask {

    private final HazardService hazardService;
    private final ApprovalService approvalService;

    /**
     * 每 30 分钟检查一次超期工单和超时审批
     */
    @Scheduled(fixedRate = 30 * 60 * 1000L)
    public void processTimeouts() {
        log.debug("开始检查超期工单...");

        // 1. 处理超期隐患工单
        try {
            List<HazardWorkOrder> overdueOrders = hazardService.getOverdueOrders();
            if (!overdueOrders.isEmpty()) {
                log.info("发现 {} 个超期工单（已自动升级）", overdueOrders.size());
            }
        } catch (Exception e) {
            log.error("处理超期工单失败", e);
        }

        // 2. 处理超时审批（自动 escalate/pass/reject）
        try {
            int processed = approvalService.processTimeoutApprovals();
            if (processed > 0) {
                log.info("自动处理 {} 个超时审批节点", processed);
            }
        } catch (Exception e) {
            log.error("处理超时审批失败", e);
        }
    }
}
