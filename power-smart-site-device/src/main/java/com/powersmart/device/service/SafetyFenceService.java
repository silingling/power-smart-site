package com.powersmart.device.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.device.entity.FenceAlertEvent;
import com.powersmart.device.entity.SafetyFence;
import com.powersmart.device.mapper.FenceAlertEventMapper;
import com.powersmart.device.mapper.SafetyFenceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 安全围栏业务服务 — CRUD + 围栏管理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SafetyFenceService {

    private final SafetyFenceMapper fenceMapper;
    private final FenceAlertEventMapper eventMapper;
    private final FenceEngineService fenceEngine;

    // ===================== 围栏 CRUD =====================

    public Page<SafetyFence> queryPage(Map<String, Object> params) {
        int pageNum = 1, pageSize = 20;
        if (params != null) {
            if (params.containsKey("page")) pageNum = Integer.parseInt(params.get("page").toString());
            if (params.containsKey("pageSize") || params.containsKey("limit"))
                pageSize = Integer.parseInt(params.getOrDefault("pageSize", params.getOrDefault("limit", "20")).toString());
        }
        Page<SafetyFence> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SafetyFence> wrapper = new LambdaQueryWrapper<>();
        if (params != null) {
            if (params.containsKey("projectId"))
                wrapper.eq(SafetyFence::getProjectId, Long.valueOf(params.get("projectId").toString()));
            if (params.containsKey("fenceType"))
                wrapper.eq(SafetyFence::getFenceType, params.get("fenceType").toString());
            if (params.containsKey("enabled"))
                wrapper.eq(SafetyFence::getEnabled, Integer.valueOf(params.get("enabled").toString()));
            if (params.containsKey("keyword")) {
                String kw = "%" + params.get("keyword") + "%";
                wrapper.and(w -> w.like(SafetyFence::getFenceName, kw)
                        .or().like(SafetyFence::getDescription, kw));
            }
        }
        wrapper.orderByDesc(SafetyFence::getCreatedAt);
        return fenceMapper.selectPage(page, wrapper);
    }

    public SafetyFence getById(Long id) {
        return fenceMapper.selectById(id);
    }

    public void add(SafetyFence fence) {
        if (fence.getEnabled() == null) fence.setEnabled(1);
        fenceMapper.insert(fence);
    }

    public void update(SafetyFence fence) {
        fenceMapper.updateById(fence);
    }

    public void delete(Long id) {
        fenceMapper.deleteById(id);
        fenceEngine.clearFenceState(id);
    }

    public void toggleEnabled(Long id, Integer enabled) {
        SafetyFence fence = new SafetyFence();
        fence.setId(id);
        fence.setEnabled(enabled);
        fenceMapper.updateById(fence);
        if (enabled == 0) {
            fenceEngine.clearFenceState(id);
        }
    }

    public List<SafetyFence> getActiveFences(Long projectId) {
        return fenceMapper.selectActiveByProject(projectId);
    }

    // ===================== 告警事件 =====================

    public Page<FenceAlertEvent> queryEventPage(Map<String, Object> params) {
        int pageNum = 1, pageSize = 20;
        if (params != null) {
            if (params.containsKey("page")) pageNum = Integer.parseInt(params.get("page").toString());
            if (params.containsKey("pageSize") || params.containsKey("limit"))
                pageSize = Integer.parseInt(params.getOrDefault("pageSize", params.getOrDefault("limit", "20")).toString());
        }
        Page<FenceAlertEvent> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FenceAlertEvent> wrapper = new LambdaQueryWrapper<>();
        if (params != null) {
            if (params.containsKey("fenceId"))
                wrapper.eq(FenceAlertEvent::getFenceId, Long.valueOf(params.get("fenceId").toString()));
            if (params.containsKey("projectId"))
                wrapper.eq(FenceAlertEvent::getProjectId, Long.valueOf(params.get("projectId").toString()));
            if (params.containsKey("eventType"))
                wrapper.eq(FenceAlertEvent::getEventType, params.get("eventType").toString());
            if (params.containsKey("targetType"))
                wrapper.eq(FenceAlertEvent::getTargetType, params.get("targetType").toString());
            if (params.containsKey("targetId"))
                wrapper.eq(FenceAlertEvent::getTargetId, params.get("targetId").toString());
            if (params.containsKey("status"))
                wrapper.eq(FenceAlertEvent::getStatus, params.get("status").toString());
        }
        wrapper.orderByDesc(FenceAlertEvent::getCreatedAt);
        return eventMapper.selectPage(page, wrapper);
    }

    public FenceAlertEvent getEventById(Long id) {
        return eventMapper.selectById(id);
    }

    public void processEvent(Long id, String processedBy) {
        FenceAlertEvent event = eventMapper.selectById(id);
        if (event == null) return;
        event.setStatus("processed");
        event.setProcessedBy(processedBy);
        event.setProcessedAt(LocalDateTime.now());
        eventMapper.updateById(event);
    }

    public void ignoreEvent(Long id) {
        FenceAlertEvent event = eventMapper.selectById(id);
        if (event == null) return;
        event.setStatus("ignored");
        eventMapper.updateById(event);
    }

    // ===================== 定位检测 =====================

    /**
     * 检查定位点是否在围栏内
     */
    public Map<String, Object> checkPointInFences(Long projectId, BigDecimal lat, BigDecimal lng) {
        List<SafetyFence> fences = fenceMapper.selectActiveByProject(projectId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("lat", lat);
        result.put("lng", lng);
        result.put("totalFences", fences.size());

        List<Map<String, Object>> fenceResults = new java.util.ArrayList<>();
        for (SafetyFence fence : fences) {
            Map<String, Object> fr = new LinkedHashMap<>();
            fr.put("fenceId", fence.getId());
            fr.put("fenceName", fence.getFenceName());
            fr.put("inside", fenceEngine.isInside(fence, lat, lng));
            fenceResults.add(fr);
        }
        result.put("fenceResults", fenceResults);
        return result;
    }

    // ===================== 构建辅助 =====================

    public SafetyFence buildFromParams(SafetyFence existing, Map<String, Object> p) {
        SafetyFence f = existing != null ? existing : new SafetyFence();
        if (existing == null && p.containsKey("enabled")) f.setEnabled(1);

        if (p.containsKey("projectId")) f.setProjectId(safeLong(p.get("projectId")));
        if (p.containsKey("fenceName")) f.setFenceName(p.get("fenceName").toString());
        if (p.containsKey("fenceType")) f.setFenceType(p.get("fenceType").toString());
        if (p.containsKey("color")) f.setColor(p.get("color").toString());
        if (p.containsKey("description")) f.setDescription(p.get("description").toString());
        if (p.containsKey("alertLevel")) f.setAlertLevel(p.get("alertLevel").toString());
        if (p.containsKey("enabled")) f.setEnabled(Integer.parseInt(p.get("enabled").toString()));
        if (p.containsKey("createBy")) f.setCreateBy(p.get("createBy").toString());

        // 圆形参数
        if (p.containsKey("centerLat")) f.setCenterLat(new BigDecimal(p.get("centerLat").toString()));
        if (p.containsKey("centerLng")) f.setCenterLng(new BigDecimal(p.get("centerLng").toString()));
        if (p.containsKey("radiusM")) f.setRadiusM(new BigDecimal(p.get("radiusM").toString()));

        // 多边形参数
        if (p.containsKey("polygonPoints")) f.setPolygonPoints(p.get("polygonPoints").toString());

        return f;
    }

    public Map<String, Object> toMap(SafetyFence f) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", f.getId());
        m.put("projectId", f.getProjectId());
        m.put("fenceName", f.getFenceName());
        m.put("fenceType", f.getFenceType());
        m.put("color", f.getColor());
        m.put("description", f.getDescription());
        m.put("centerLat", f.getCenterLat());
        m.put("centerLng", f.getCenterLng());
        m.put("radiusM", f.getRadiusM());
        m.put("polygonPoints", f.getPolygonPoints());
        m.put("alertLevel", f.getAlertLevel());
        m.put("enabled", f.getEnabled());
        m.put("createBy", f.getCreateBy());
        m.put("createdAt", f.getCreatedAt() != null ? f.getCreatedAt().toString() : "");
        m.put("updatedAt", f.getUpdatedAt() != null ? f.getUpdatedAt().toString() : "");
        return m;
    }

    private Long safeLong(Object v) {
        if (v == null) return null;
        try { return Long.parseLong(v.toString()); } catch (NumberFormatException e) { return null; }
    }
}
