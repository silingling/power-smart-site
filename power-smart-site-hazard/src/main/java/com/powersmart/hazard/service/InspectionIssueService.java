package com.powersmart.hazard.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.annotation.OperateLog;
import com.powersmart.common.auth.SecurityContext;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.util.PageHelper;
import com.powersmart.hazard.entity.HazardReport;
import com.powersmart.hazard.entity.InspectionIssue;
import com.powersmart.hazard.mapper.HazardReportMapper;
import com.powersmart.hazard.mapper.InspectionIssueMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@OperateLog(module = "巡检问题管理")
public class InspectionIssueService {

    private final InspectionIssueMapper inspectionIssueMapper;
    private final HazardReportMapper hazardReportMapper;

    public PageResult<InspectionIssue> list(Map<String, Object> params) {
        Page<InspectionIssue> page = PageHelper.of(params);
        LambdaQueryWrapper<InspectionIssue> wrapper = new LambdaQueryWrapper<>();
        if (params != null) {
            if (params.containsKey("taskId") && params.get("taskId") != null)
                wrapper.eq(InspectionIssue::getTaskId, Long.valueOf(params.get("taskId").toString()));
            if (params.containsKey("issueLevel") && params.get("issueLevel") != null)
                wrapper.eq(InspectionIssue::getIssueLevel, params.get("issueLevel").toString());
            if (params.containsKey("status") && params.get("status") != null)
                wrapper.eq(InspectionIssue::getStatus, params.get("status").toString());
        }
        wrapper.orderByDesc(InspectionIssue::getCreatedAt);
        return PageResult.from(inspectionIssueMapper.selectPage(page, wrapper));
    }

    public InspectionIssue getById(Long id) {
        return inspectionIssueMapper.selectById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    @OperateLog(description = "新增巡检问题")
    public void add(InspectionIssue entity) {
        if (StrUtil.isBlank(entity.getIssueDesc())) {
            throw new IllegalArgumentException("问题描述不能为空");
        }
        if (StrUtil.isBlank(entity.getIssueLevel())) {
            throw new IllegalArgumentException("问题级别不能为空");
        }
        entity.setStatus("pending");
        inspectionIssueMapper.insert(entity);

        // 重大问题/危急问题自动创建隐患报告
        if ("critical".equals(entity.getIssueLevel())) {
            createHazardReport(entity);
        }
    }

    @OperateLog(description = "修改巡检问题")
    public void update(InspectionIssue entity) {
        InspectionIssue existing = inspectionIssueMapper.selectById(entity.getId());
        if (existing == null) {
            throw new IllegalArgumentException("巡检问题不存在");
        }
        if (StrUtil.isNotBlank(entity.getIssueDesc())) existing.setIssueDesc(entity.getIssueDesc());
        if (StrUtil.isNotBlank(entity.getIssueType())) existing.setIssueType(entity.getIssueType());
        if (StrUtil.isNotBlank(entity.getIssueLevel())) existing.setIssueLevel(entity.getIssueLevel());
        if (entity.getPhotoJson() != null) existing.setPhotoJson(entity.getPhotoJson());
        if (StrUtil.isNotBlank(entity.getLocation())) existing.setLocation(entity.getLocation());
        if (StrUtil.isNotBlank(entity.getStatus())) existing.setStatus(entity.getStatus());
        inspectionIssueMapper.updateById(existing);
    }

    @OperateLog(description = "删除巡检问题")
    public void delete(Long id) {
        inspectionIssueMapper.deleteById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    @OperateLog(description = "更新问题处理状态")
    public void updateStatus(Long id, String status, Long handlerId, String handlerName, String handleMeasure) {
        InspectionIssue issue = inspectionIssueMapper.selectById(id);
        if (issue == null) {
            throw new IllegalArgumentException("巡检问题不存在");
        }

        // 状态流转校验: pending → rectifying → resolved → verified
        String currentStatus = issue.getStatus();
        if ("pending".equals(currentStatus) && !"rectifying".equals(status)) {
            throw new IllegalArgumentException("待处理状态只能变更为整改中");
        }
        if ("rectifying".equals(currentStatus) && !"resolved".equals(status) && !"pending".equals(status)) {
            throw new IllegalArgumentException("整改中状态只能变更为已解决或退回待处理");
        }
        if ("resolved".equals(currentStatus) && !"verified".equals(status)) {
            throw new IllegalArgumentException("已解决状态只能变更为已验收");
        }
        if ("verified".equals(currentStatus)) {
            throw new IllegalArgumentException("已验收问题不可变更");
        }

        issue.setStatus(status);

        if ("rectifying".equals(status)) {
            issue.setHandlerId(handlerId);
            issue.setHandlerName(handlerName);
            issue.setHandleMeasure(handleMeasure);
        } else if ("resolved".equals(status)) {
            issue.setHandledAt(LocalDateTime.now());
            if (StrUtil.isNotBlank(handleMeasure)) {
                issue.setHandleMeasure(handleMeasure);
            }
        } else if ("verified".equals(status)) {
            issue.setVerifierId(handlerId);
            issue.setVerifierName(handlerName);
            issue.setVerifiedAt(LocalDateTime.now());
        }

        inspectionIssueMapper.updateById(issue);
        log.info("Issue [{}] status updated: {} → {}", id, currentStatus, status);
    }

    /**
     * 为重大问题创建隐患报告
     */
    private void createHazardReport(InspectionIssue issue) {
        try {
            HazardReport report = new HazardReport();
            report.setProjectId(null); // projectId will be populated from related task if available
            report.setReportType(2); // 人工上报
            report.setHazardType(issue.getIssueType() != null ? issue.getIssueType() : "巡检问题");
            report.setHazardLevel(3); // 重大
            report.setDescription(issue.getIssueDesc());
            report.setLocation(issue.getLocation());
            report.setReportedBy(SecurityContext.getCurrentUserId());
            report.setStatus(1); // 待整改
            hazardReportMapper.insert(report);

            // 关联隐患报告ID
            issue.setHazardReportId(report.getId());
            inspectionIssueMapper.updateById(issue);
            log.info("Created hazard report [{}] for critical issue [{}]", report.getId(), issue.getId());
        } catch (Exception e) {
            log.error("Failed to create hazard report for critical issue [{}]", issue.getId(), e);
        }
    }
}
