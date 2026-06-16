package com.powersmart.hazard.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.constant.ApiConstant;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.common.util.PageHelper;
import com.powersmart.hazard.entity.AiDetectionCallback;
import com.powersmart.hazard.entity.AiViolation;
import com.powersmart.hazard.entity.HazardReport;
import com.powersmart.hazard.mapper.AiDetectionCallbackMapper;
import com.powersmart.hazard.mapper.AiViolationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * AI 检测回调 服务层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiDetectionService {

    private final AiDetectionCallbackMapper callbackMapper;
    private final AiViolationMapper aiViolationMapper;
    private final HazardService hazardService;

    /**
     * 接收外部 AI 检测回调，保存到 ai_detection_callback
     */
    @Transactional(rollbackFor = Exception.class)
    public AiDetectionCallback processCallback(Map<String, Object> callbackData) {
        AiDetectionCallback callback = new AiDetectionCallback();
        callback.setCallbackId(callbackData.getOrDefault("callbackId", callbackData.getOrDefault("id", "")).toString());
        if (callbackData.containsKey("projectId") && callbackData.get("projectId") != null)
            callback.setProjectId(Long.valueOf(callbackData.get("projectId").toString()));
        if (callbackData.containsKey("cameraId") && callbackData.get("cameraId") != null)
            callback.setCameraId(Long.valueOf(callbackData.get("cameraId").toString()));
        callback.setViolationType(callbackData.getOrDefault("violationType", callbackData.getOrDefault("eventType", "")).toString());
        if (callbackData.containsKey("confidence") && callbackData.get("confidence") != null)
            callback.setConfidence(Double.valueOf(callbackData.get("confidence").toString()));
        callback.setSnapshotUrl(callbackData.getOrDefault("snapshotUrl", "").toString());
        callback.setVideoUrl(callbackData.getOrDefault("videoUrl", "").toString());
        callback.setCallbackRaw(callbackData.toString());
        callback.setMatchedRule(callbackData.getOrDefault("matchedRule", callbackData.getOrDefault("rule", "")).toString());
        callback.setProcessed(false);
        callback.setCreatedAt(LocalDateTime.now());
        callbackMapper.insert(callback);
        log.info("AI detection callback saved, id={}, type={}", callback.getId(), callback.getViolationType());
        return callback;
    }

    /**
     * 确认检测结果 → 创建 AiViolation + 自动上报隐患
     */
    @Transactional(rollbackFor = Exception.class)
    public void confirmDetection(Long callbackId) {
        AiDetectionCallback callback = callbackMapper.selectById(callbackId);
        if (callback == null) {
            throw new RuntimeException("回调记录不存在，id=" + callbackId);
        }
        if (Boolean.TRUE.equals(callback.getProcessed())) {
            throw new RuntimeException("回调记录已处理，id=" + callbackId);
        }

        // 1. 创建 AiViolation
        AiViolation violation = new AiViolation();
        violation.setProjectId(callback.getProjectId());
        violation.setCameraId(callback.getCameraId());
        violation.setViolationType(callback.getViolationType());
        violation.setConfidence(callback.getConfidence());
        violation.setSnapshotUrl(callback.getSnapshotUrl());
        violation.setVideoUrl(callback.getVideoUrl());
        violation.setCallbackId(callback.getId());
        violation.setStatus(0); // 未处理
        violation.setCreatedAt(LocalDateTime.now());
        aiViolationMapper.insert(violation);

        // 2. 自动上报隐患
        HazardReport report = new HazardReport();
        report.setProjectId(callback.getProjectId());
        report.setReportType(ApiConstant.ReportType.AI); // AI 上报
        report.setHazardType(callback.getViolationType());
        report.setHazardLevel(ApiConstant.HazardLevel.GENERAL);
        report.setDescription("AI 自动检测：" + callback.getViolationType());
        report.setImageUrl(callback.getSnapshotUrl());
        report.setVideoUrl(callback.getVideoUrl());
        report.setStatus(ApiConstant.HazardStatus.PENDING);
        HazardReport saved = hazardService.reportHazard(report, null);

        // 3. 更新回调状态
        callback.setProcessed(true);
        callback.setHazardId(saved.getId());
        callbackMapper.updateById(callback);

        log.info("AI detection confirmed, callbackId={}, violationId={}, hazardId={}",
                callbackId, violation.getId(), saved.getId());
    }

    /**
     * 标记检测结果为误报
     */
    @Transactional(rollbackFor = Exception.class)
    public void dismissDetection(Long callbackId) {
        AiDetectionCallback callback = callbackMapper.selectById(callbackId);
        if (callback == null) {
            throw new RuntimeException("回调记录不存在，id=" + callbackId);
        }
        if (Boolean.TRUE.equals(callback.getProcessed())) {
            throw new RuntimeException("回调记录已处理，id=" + callbackId);
        }
        callback.setProcessed(true);
        callback.setErrorMsg("误报 - 人工确认");
        callbackMapper.updateById(callback);
        log.info("AI detection dismissed as false alarm, callbackId={}", callbackId);
    }

    /**
     * 分页查询回调列表
     */
    public PageResult<AiDetectionCallback> getCallbackList(Map<String, Object> params) {
        Page<AiDetectionCallback> page = PageHelper.of(params);
        LambdaQueryWrapper<AiDetectionCallback> wrapper = new LambdaQueryWrapper<>();
        if (params != null) {
            if (params.containsKey("projectId") && params.get("projectId") != null)
                wrapper.eq(AiDetectionCallback::getProjectId, Long.valueOf(params.get("projectId").toString()));
            if (params.containsKey("cameraId") && params.get("cameraId") != null)
                wrapper.eq(AiDetectionCallback::getCameraId, Long.valueOf(params.get("cameraId").toString()));
            if (params.containsKey("violationType") && params.get("violationType") != null && StrUtil.isNotBlank(params.get("violationType").toString()))
                wrapper.eq(AiDetectionCallback::getViolationType, params.get("violationType").toString());
            if (params.containsKey("processed") && params.get("processed") != null)
                wrapper.eq(AiDetectionCallback::getProcessed, Boolean.valueOf(params.get("processed").toString()));
        }
        wrapper.orderByDesc(AiDetectionCallback::getCreatedAt);
        return PageResult.from(callbackMapper.selectPage(page, wrapper));
    }
}
