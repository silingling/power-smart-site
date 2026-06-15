package com.powersmart.hazard.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.powersmart.common.exception.BusinessException;
import com.powersmart.hazard.entity.*;
import com.powersmart.hazard.mapper.*;
import com.powersmart.hazard.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审批流引擎实现
 *
 * <p>审批流程基于 approval_node 配置驱动：
 * 1. 风险等级 ≥ 重大 → 自动启动 3 级审批链
 * 2. 每个节点由对应的 role_key 角色处理
 * 3. 超时自动升级（escalate）或跳过
 * 4. 所有操作记录写入 approval_record
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalServiceImpl implements ApprovalService {

    private static final String BIZ_TYPE_HAZARD = "hazard";

    private final ApprovalNodeMapper nodeMapper;
    private final ApprovalRecordMapper recordMapper;
    private final HazardApprovalMapper hazardApprovalMapper;
    private final HazardReportMapper hazardReportMapper;
    private final HazardWorkOrderMapper workOrderMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startApproval(Long hazardId) {
        HazardReport report = hazardReportMapper.selectById(hazardId);
        if (report == null) throw new BusinessException("隐患不存在");

        // 只有重大(2)和特别重大(3)需要多级审批
        if (report.getHazardLevel() == null || report.getHazardLevel() < 2) {
            log.info("隐患级别一般({})，无需审批流程: hazardId={}", report.getHazardLevel(), hazardId);
            return;
        }

        List<ApprovalNode> nodes = nodeMapper.selectByBizType(BIZ_TYPE_HAZARD);
        if (CollUtil.isEmpty(nodes)) {
            log.warn("未配置审批节点，跳过审批: hazardId={}", hazardId);
            return;
        }

        // 创建/更新审批流程记录
        HazardApproval ha = hazardApprovalMapper.selectOne(
                new LambdaQueryWrapper<HazardApproval>().eq(HazardApproval::getHazardId, hazardId));
        if (ha == null) {
            ha = new HazardApproval();
            ha.setHazardId(hazardId);
        }
        ha.setCurrentNode(1);
        ha.setTotalNodes(nodes.size());
        ha.setApprovalStatus("approving");
        ha.setStartedAt(LocalDateTime.now());
        ha.setEscalated(0);

        if (ha.getId() == null) {
            hazardApprovalMapper.insert(ha);
        } else {
            hazardApprovalMapper.updateById(ha);
        }

        log.info("审批流程启动: hazardId={}, 总节点={}", hazardId, nodes.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApprovalRecord processApproval(Long hazardId, String action,
                                           Long operatorId, String operatorName, String comment) {
        HazardApproval ha = hazardApprovalMapper.selectOne(
                new LambdaQueryWrapper<HazardApproval>().eq(HazardApproval::getHazardId, hazardId));
        if (ha == null) {
            throw new BusinessException("该隐患未启动审批流程");
        }
        if ("approved".equals(ha.getApprovalStatus()) || "rejected".equals(ha.getApprovalStatus())) {
            throw new BusinessException("审批已完结，无法重复操作");
        }

        List<ApprovalNode> nodes = nodeMapper.selectByBizType(BIZ_TYPE_HAZARD);
        if (CollUtil.isEmpty(nodes)) {
            throw new BusinessException("未配置审批节点");
        }

        // 找到当前节点
        ApprovalNode currentNode = nodes.stream()
                .filter(n -> n.getNodeOrder().equals(ha.getCurrentNode()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("未找到当前审批节点"));

        // 记录审批操作
        ApprovalRecord record = new ApprovalRecord();
        record.setBizType(BIZ_TYPE_HAZARD);
        record.setBizId(hazardId);
        record.setNodeId(currentNode.getId());
        record.setNodeName(currentNode.getNodeName());
        record.setAction(action);
        record.setOperatorId(operatorId);
        record.setOperatorName(operatorName);
        record.setComment(comment);
        recordMapper.insert(record);

        if ("reject".equals(action)) {
            // 驳回：审批终止
            ha.setApprovalStatus("rejected");
            ha.setFinishedAt(LocalDateTime.now());
            hazardApprovalMapper.updateById(ha);

            // 更新隐患状态为待整改
            HazardReport report = hazardReportMapper.selectById(hazardId);
            if (report != null) {
                report.setStatus(1); // 待整改
                hazardReportMapper.updateById(report);
            }
            log.info("审批驳回: hazardId={}, node={}", hazardId, currentNode.getNodeName());
        } else {
            // 通过：推进到下一节点
            if (ha.getCurrentNode() >= ha.getTotalNodes()) {
                // 所有节点已通过，审批完成
                ha.setApprovalStatus("approved");
                ha.setFinishedAt(LocalDateTime.now());
                hazardApprovalMapper.updateById(ha);

                // 更新隐患状态
                HazardReport report = hazardReportMapper.selectById(hazardId);
                if (report != null) {
                    report.setStatus(3); // 已验收
                    hazardReportMapper.updateById(report);
                }
                log.info("审批全部通过: hazardId={}", hazardId);
            } else {
                // 推进到下一节点
                ha.setCurrentNode(ha.getCurrentNode() + 1);
                hazardApprovalMapper.updateById(ha);
                log.info("审批节点通过: hazardId={}, node={} → {}", hazardId,
                        currentNode.getNodeName(),
                        nodes.stream().filter(n -> n.getNodeOrder().equals(ha.getCurrentNode()))
                                .map(ApprovalNode::getNodeName).findFirst().orElse(""));
            }
        }

        return record;
    }

    @Override
    public List<ApprovalRecord> getApprovalHistory(Long hazardId) {
        return recordMapper.selectList(new LambdaQueryWrapper<ApprovalRecord>()
                .eq(ApprovalRecord::getBizType, BIZ_TYPE_HAZARD)
                .eq(ApprovalRecord::getBizId, hazardId)
                .orderByAsc(ApprovalRecord::getCreatedAt));
    }

    @Override
    public String getCurrentNodeName(Long hazardId) {
        HazardApproval ha = hazardApprovalMapper.selectOne(
                new LambdaQueryWrapper<HazardApproval>().eq(HazardApproval::getHazardId, hazardId));
        if (ha == null) return null;
        if ("approved".equals(ha.getApprovalStatus())) return "审批已通过";
        if ("rejected".equals(ha.getApprovalStatus())) return "审批已驳回";

        ApprovalNode node = nodeMapper.selectByBizTypeAndOrder(BIZ_TYPE_HAZARD, ha.getCurrentNode());
        return node != null ? node.getNodeName() : "未知节点";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int processTimeoutApprovals() {
        // 查找所有超时的审批
        List<ApprovalNode> nodes = nodeMapper.selectByBizType(BIZ_TYPE_HAZARD);
        if (CollUtil.isEmpty(nodes)) return 0;

        int processed = 0;
        for (ApprovalNode node : nodes) {
            if (node.getTimeoutHours() == null || node.getTimeoutHours() <= 0) continue;

            // 查找当前节点超时的审批
            List<HazardApproval> timeoutApprovals = hazardApprovalMapper.selectList(
                    new LambdaQueryWrapper<HazardApproval>()
                            .eq(HazardApproval::getApprovalStatus, "approving")
                            .eq(HazardApproval::getCurrentNode, node.getNodeOrder())
                            .lt(HazardApproval::getUpdatedAt,
                                    LocalDateTime.now().minusHours(node.getTimeoutHours())));

            for (HazardApproval ha : timeoutApprovals) {
                String autoAction = node.getAutoAction() != null ? node.getAutoAction() : "escalate";
                log.warn("审批超时: hazardId={}, node={}, autoAction={}",
                        ha.getHazardId(), node.getNodeName(), autoAction);

                switch (autoAction) {
                    case "pass":
                        processApproval(ha.getHazardId(), "pass", 0L, "系统", "超时自动通过");
                        break;
                    case "reject":
                        processApproval(ha.getHazardId(), "reject", 0L, "系统", "超时自动驳回");
                        break;
                    default: // escalate
                        ha.setEscalated(1);
                        ha.setApprovalStatus("escalated");
                        hazardApprovalMapper.updateById(ha);

                        // 记录升级操作
                        ApprovalRecord record = new ApprovalRecord();
                        record.setBizType(BIZ_TYPE_HAZARD);
                        record.setBizId(ha.getHazardId());
                        record.setNodeId(node.getId());
                        record.setNodeName(node.getNodeName());
                        record.setAction("escalate");
                        record.setOperatorId(0L);
                        record.setOperatorName("系统");
                        record.setComment("超时自动升级");
                        recordMapper.insert(record);
                        break;
                }
                processed++;
            }
        }
        if (processed > 0) log.info("超时审批处理完成: count={}", processed);
        return processed;
    }
}
