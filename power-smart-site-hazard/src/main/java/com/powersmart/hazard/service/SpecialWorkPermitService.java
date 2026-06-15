package com.powersmart.hazard.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.entity.Result;
import com.powersmart.common.util.PageHelper;
import com.powersmart.hazard.entity.PermitCheckItem;
import com.powersmart.hazard.entity.SpecialWorkPermit;
import com.powersmart.hazard.mapper.PermitCheckItemMapper;
import com.powersmart.hazard.mapper.SpecialWorkPermitMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 特种作业票业务服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpecialWorkPermitService {

    private final SpecialWorkPermitMapper permitMapper;
    private final PermitCheckItemMapper checkItemMapper;

    // ===================== 作业票类型定义 =====================

    public static final Map<String, String> PERMIT_TYPE_NAMES = new LinkedHashMap<>();
    static {
        PERMIT_TYPE_NAMES.put("hot_work", "动火作业票");
        PERMIT_TYPE_NAMES.put("height_work", "高处作业票");
        PERMIT_TYPE_NAMES.put("confined_space", "受限空间作业票");
        PERMIT_TYPE_NAMES.put("temp_electric", "临时用电作业票");
        PERMIT_TYPE_NAMES.put("lifting", "吊装作业票");
        PERMIT_TYPE_NAMES.put("excavation", "动土/开挖作业票");
        PERMIT_TYPE_NAMES.put("road_blockage", "断路作业票");
    }

    public static final Map<String, String> STATUS_NAMES = new LinkedHashMap<>();
    static {
        STATUS_NAMES.put("draft", "草稿");
        STATUS_NAMES.put("submitted", "待审核");
        STATUS_NAMES.put("safety_review", "安全审核中");
        STATUS_NAMES.put("approved", "已签发");
        STATUS_NAMES.put("active", "进行中");
        STATUS_NAMES.put("completed", "已完工");
        STATUS_NAMES.put("closed", "已归档");
        STATUS_NAMES.put("rejected", "已驳回");
        STATUS_NAMES.put("cancelled", "已作废");
    }

    // ===================== CRUD =====================

    public Page<SpecialWorkPermit> queryPage(Map<String, Object> params) {
        Page<SpecialWorkPermit> pageParam = PageHelper.of(params);
        LambdaQueryWrapper<SpecialWorkPermit> wrapper = new LambdaQueryWrapper<>();
        if (params != null) {
            if (params.containsKey("projectId"))
                wrapper.eq(SpecialWorkPermit::getProjectId, safeLong(params.get("projectId")));
            if (params.containsKey("permitType"))
                wrapper.eq(SpecialWorkPermit::getPermitType, params.get("permitType").toString());
            if (params.containsKey("status"))
                wrapper.eq(SpecialWorkPermit::getStatus, params.get("status").toString());
            if (params.containsKey("applicantId"))
                wrapper.eq(SpecialWorkPermit::getApplicantId, safeLong(params.get("applicantId")));
            if (StrUtil.isNotBlank(safeStr(params.get("keyword")))) {
                String kw = "%" + params.get("keyword") + "%";
                wrapper.and(w -> w.like(SpecialWorkPermit::getTitle, kw)
                        .or().like(SpecialWorkPermit::getPermitNo, kw)
                        .or().like(SpecialWorkPermit::getWorkLocation, kw)
                        .or().like(SpecialWorkPermit::getApplicantName, kw));
            }
            // 时间范围
            if (params.containsKey("startDate"))
                wrapper.ge(SpecialWorkPermit::getExpectedStartTime, params.get("startDate").toString());
            if (params.containsKey("endDate"))
                wrapper.le(SpecialWorkPermit::getExpectedEndTime, params.get("endDate").toString());
        }
        wrapper.orderByDesc(SpecialWorkPermit::getCreatedAt);
        return permitMapper.selectPage(pageParam, wrapper);
    }

    public SpecialWorkPermit getById(Long id) {
        return permitMapper.selectById(id);
    }

    public void add(SpecialWorkPermit permit) {
        if (permit.getStatus() == null) permit.setStatus("draft");
        permit.setPermitNo(generatePermitNo());
        permitMapper.insert(permit);
    }

    public void update(SpecialWorkPermit permit) {
        permitMapper.updateById(permit);
    }

    public void delete(Long id) {
        permitMapper.deleteById(id);
    }

    // ===================== 状态流转 =====================

    /** 提交审批: draft → submitted */
    public Result<Void> submit(Long id) {
        SpecialWorkPermit p = permitMapper.selectById(id);
        if (p == null) return Result.fail("作业票不存在");
        if (!"draft".equals(p.getStatus())) return Result.fail("当前状态不可提交");
        p.setStatus("submitted");
        p.setCurrentNode("safety_review");
        permitMapper.updateById(p);
        return Result.ok();
    }

    /** 安全审核通过: submitted → approved(已签发) */
    public Result<Void> approve(Long id, String reviewerName, String issuerSignature) {
        SpecialWorkPermit p = permitMapper.selectById(id);
        if (p == null) return Result.fail("作业票不存在");
        if (!"submitted".equals(p.getStatus())) return Result.fail("当前状态不可审核通过");
        p.setStatus("approved");
        p.setCurrentNode("approved");
        p.setIssuerName(reviewerName);
        p.setIssueTime(LocalDateTime.now());
        if (issuerSignature != null) p.setIssuerSignature(issuerSignature);
        permitMapper.updateById(p);
        return Result.ok();
    }

    /** 驳回: submitted/safety_review → rejected */
    public Result<Void> reject(Long id, String reason) {
        SpecialWorkPermit p = permitMapper.selectById(id);
        if (p == null) return Result.fail("作业票不存在");
        if (!"submitted".equals(p.getStatus()) && !"safety_review".equals(p.getStatus()))
            return Result.fail("当前状态不可驳回");
        p.setStatus("rejected");
        p.setCurrentNode("rejected");
        p.setRejectReason(reason);
        permitMapper.updateById(p);
        return Result.ok();
    }

    /** 开始作业: approved → active */
    public Result<Void> startWork(Long id) {
        SpecialWorkPermit p = permitMapper.selectById(id);
        if (p == null) return Result.fail("作业票不存在");
        if (!"approved".equals(p.getStatus())) return Result.fail("作业票尚未签发，不能开始作业");
        p.setStatus("active");
        p.setCurrentNode("active");
        permitMapper.updateById(p);
        return Result.ok();
    }

    /** 完工: active → completed */
    public Result<Void> complete(Long id, String completionNote, String closerName, String closerSignature) {
        SpecialWorkPermit p = permitMapper.selectById(id);
        if (p == null) return Result.fail("作业票不存在");
        if (!"active".equals(p.getStatus())) return Result.fail("当前状态不可完工");
        p.setStatus("completed");
        p.setCurrentNode("completed");
        p.setActualEndTime(LocalDateTime.now());
        p.setCompletionNote(completionNote);
        p.setCloserName(closerName);
        p.setCloseTime(LocalDateTime.now());
        if (closerSignature != null) p.setCloserSignature(closerSignature);
        permitMapper.updateById(p);
        return Result.ok();
    }

    /** 归档: completed → closed */
    public Result<Void> close(Long id, String closerName) {
        SpecialWorkPermit p = permitMapper.selectById(id);
        if (p == null) return Result.fail("作业票不存在");
        if (!"completed".equals(p.getStatus())) return Result.fail("仅完工后的作业票可归档");
        p.setStatus("closed");
        p.setCurrentNode("closed");
        p.setCloserName(closerName);
        p.setCloseTime(LocalDateTime.now());
        permitMapper.updateById(p);
        return Result.ok();
    }

    /** 作废: 任意非终态 → cancelled */
    public Result<Void> cancel(Long id, String reason) {
        SpecialWorkPermit p = permitMapper.selectById(id);
        if (p == null) return Result.fail("作业票不存在");
        if (List.of("closed", "cancelled").contains(p.getStatus()))
            return Result.fail("已归档或已作废的作业票不可取消");
        p.setStatus("cancelled");
        p.setCurrentNode("cancelled");
        p.setRejectReason(reason);
        p.setCloseTime(LocalDateTime.now());
        permitMapper.updateById(p);
        return Result.ok();
    }

    // ===================== 延期 =====================

    /** 延期作业 */
    public Result<Void> extendPermit(Long id, LocalDateTime newEndTime, String reason) {
        SpecialWorkPermit p = permitMapper.selectById(id);
        if (p == null) return Result.fail("作业票不存在");
        if (!"active".equals(p.getStatus()) && !"approved".equals(p.getStatus()))
            return Result.fail("当前状态不可延期");
        p.setNewEndTime(newEndTime);
        p.setExtensionReason(reason);
        p.setExtendedCount(p.getExtendedCount() == null ? 1 : p.getExtendedCount() + 1);
        permitMapper.updateById(p);
        return Result.ok();
    }

    // ===================== 检查项 =====================

    public List<PermitCheckItem> getCheckItems(String permitType) {
        if (permitType == null) return checkItemMapper.selectList(
                new LambdaQueryWrapper<PermitCheckItem>().eq(PermitCheckItem::getEnabled, 1).orderByAsc(PermitCheckItem::getSortOrder));
        return checkItemMapper.selectByPermitType(permitType);
    }

    public void addCheckItem(PermitCheckItem item) {
        checkItemMapper.insert(item);
    }

    public void updateCheckItem(PermitCheckItem item) {
        checkItemMapper.updateById(item);
    }

    public void deleteCheckItem(Long id) {
        checkItemMapper.deleteById(id);
    }

    // ===================== 统计 =====================

    public Map<String, Long> getStatusStats(Long projectId) {
        Map<String, Long> stats = new LinkedHashMap<>();
        for (String status : List.of("submitted", "approved", "active", "completed", "closed")) {
            stats.put(status, permitMapper.countByStatus(projectId, status));
        }
        return stats;
    }

    public List<SpecialWorkPermit> getActivePermits(Long projectId) {
        return permitMapper.selectActivePermits(projectId);
    }

    // ===================== 辅助 =====================

    /** 生成票号: SP-2026-06-15-001 */
    private String generatePermitNo() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String maxNo = permitMapper.selectMaxPermitNoToday();
        int seq = 1;
        if (maxNo != null) {
            String[] parts = maxNo.split("-");
            try { seq = Integer.parseInt(parts[parts.length - 1]) + 1; } catch (Exception ignored) {}
        }
        return String.format("SP-%s-%03d", today, seq);
    }

    /** 构建 Permit 对象（从前端参数） */
    public SpecialWorkPermit buildFromParams(SpecialWorkPermit existing, Map<String, Object> p) {
        SpecialWorkPermit wp = existing != null ? existing : new SpecialWorkPermit();
        if (p.containsKey("projectId")) wp.setProjectId(safeLong(p.get("projectId")));
        if (p.containsKey("permitType")) wp.setPermitType(p.get("permitType").toString());
        if (p.containsKey("title")) wp.setTitle(p.get("title").toString());
        if (p.containsKey("workLocation")) wp.setWorkLocation(p.get("workLocation").toString());
        if (p.containsKey("workContent")) wp.setWorkContent(p.get("workContent").toString());
        if (p.containsKey("riskAnalysis")) wp.setRiskAnalysis(p.get("riskAnalysis").toString());
        if (p.containsKey("safetyMeasures")) wp.setSafetyMeasures(p.get("safetyMeasures").toString());
        if (p.containsKey("gasTestResult")) wp.setGasTestResult(p.get("gasTestResult").toString());
        if (p.containsKey("workTeamName")) wp.setWorkTeamName(p.get("workTeamName").toString());
        if (p.containsKey("workerCount")) wp.setWorkerCount(safeInt(p.get("workerCount")));
        if (p.containsKey("applicantId")) wp.setApplicantId(safeLong(p.get("applicantId")));
        if (p.containsKey("applicantName")) wp.setApplicantName(p.get("applicantName").toString());
        if (p.containsKey("applicantDept")) wp.setApplicantDept(p.get("applicantDept").toString());
        if (p.containsKey("监护人Id")) wp.set监护人Id(safeLong(p.get("监护人Id")));
        if (p.containsKey("监护人Name")) wp.set监护人Name(p.get("监护人Name").toString());
        if (p.containsKey("负责人Id")) wp.set负责人Id(safeLong(p.get("负责人Id")));
        if (p.containsKey("负责人Name")) wp.set负责人Name(p.get("负责人Name").toString());
        if (p.containsKey("attachmentJson")) wp.setAttachmentJson(p.get("attachmentJson").toString());
        if (p.containsKey("remark")) wp.setRemark(p.get("remark").toString());
        if (p.containsKey("createBy")) wp.setCreateBy(p.get("createBy").toString());
        if (p.containsKey("applicantSignature")) wp.setApplicantSignature(p.get("applicantSignature").toString());

        // 日期解析
        if (p.containsKey("expectedStartTime"))
            wp.setExpectedStartTime(LocalDateTime.parse(p.get("expectedStartTime").toString()));
        if (p.containsKey("expectedEndTime"))
            wp.setExpectedEndTime(LocalDateTime.parse(p.get("expectedEndTime").toString()));

        return wp;
    }

    /** Entity → Map（前端友好） */
    public Map<String, Object> toMap(SpecialWorkPermit wp) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", wp.getId());
        m.put("projectId", wp.getProjectId());
        m.put("permitNo", wp.getPermitNo());
        m.put("permitType", wp.getPermitType());
        m.put("permitTypeName", PERMIT_TYPE_NAMES.getOrDefault(wp.getPermitType(), wp.getPermitType()));
        m.put("title", wp.getTitle());
        m.put("workLocation", wp.getWorkLocation());
        m.put("workContent", wp.getWorkContent());
        m.put("riskAnalysis", wp.getRiskAnalysis());
        m.put("safetyMeasures", wp.getSafetyMeasures());
        m.put("gasTestResult", wp.getGasTestResult());
        m.put("expectedStartTime", wp.getExpectedStartTime() != null ? wp.getExpectedStartTime().toString() : "");
        m.put("expectedEndTime", wp.getExpectedEndTime() != null ? wp.getExpectedEndTime().toString() : "");
        m.put("workTeamName", wp.getWorkTeamName());
        m.put("workerCount", wp.getWorkerCount());
        m.put("applicantId", wp.getApplicantId());
        m.put("applicantName", wp.getApplicantName());
        m.put("applicantDept", wp.getApplicantDept());
        m.put("监护人Id", wp.get监护人Id());
        m.put("监护人Name", wp.get监护人Name());
        m.put("负责人Id", wp.get负责人Id());
        m.put("负责人Name", wp.get负责人Name());
        m.put("status", wp.getStatus());
        m.put("statusName", STATUS_NAMES.getOrDefault(wp.getStatus(), wp.getStatus()));
        m.put("currentNode", wp.getCurrentNode());
        m.put("rejectReason", wp.getRejectReason());
        m.put("issuerId", wp.getIssuerId());
        m.put("issuerName", wp.getIssuerName());
        m.put("issueTime", wp.getIssueTime() != null ? wp.getIssueTime().toString() : "");
        m.put("actualEndTime", wp.getActualEndTime() != null ? wp.getActualEndTime().toString() : "");
        m.put("completionNote", wp.getCompletionNote());
        m.put("closerId", wp.getCloserId());
        m.put("closerName", wp.getCloserName());
        m.put("closeTime", wp.getCloseTime() != null ? wp.getCloseTime().toString() : "");
        m.put("extendedCount", wp.getExtendedCount());
        m.put("newEndTime", wp.getNewEndTime() != null ? wp.getNewEndTime().toString() : "");
        m.put("extensionReason", wp.getExtensionReason());
        m.put("applicantSignature", wp.getApplicantSignature());
        m.put("issuerSignature", wp.getIssuerSignature());
        m.put("closerSignature", wp.getCloserSignature());
        m.put("attachmentJson", wp.getAttachmentJson());
        m.put("remark", wp.getRemark());
        m.put("createBy", wp.getCreateBy());
        m.put("createdAt", wp.getCreatedAt() != null ? wp.getCreatedAt().toString() : "");
        m.put("updatedAt", wp.getUpdatedAt() != null ? wp.getUpdatedAt().toString() : "");
        return m;
    }

    // ===================== 工具方法 =====================

    private Long safeLong(Object v) {
        if (v == null) return null;
        try { return Long.parseLong(v.toString()); } catch (NumberFormatException e) { return null; }
    }
    private Integer safeInt(Object v) {
        if (v == null) return null;
        try { return Integer.parseInt(v.toString()); } catch (NumberFormatException e) { return null; }
    }
    private String safeStr(Object v) {
        return v != null && !v.toString().isEmpty() ? v.toString() : null;
    }
}
