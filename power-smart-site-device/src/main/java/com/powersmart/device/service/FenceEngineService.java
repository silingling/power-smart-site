package com.powersmart.device.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.powersmart.common.push.SsePushService;
import com.powersmart.device.entity.FenceAlertEvent;
import com.powersmart.device.entity.SafetyFence;
import com.powersmart.device.mapper.FenceAlertEventMapper;
import com.powersmart.device.mapper.SafetyFenceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 电子围栏引擎 — 核心定位检测 + 告警触发
 *
 * <p>接收定位数据 → 判断点在围栏内/外 → 比对上次状态 → 产生 enter/leave 事件。</p>
 *
 * <p>支持两种围栏形状：</p>
 * <ul>
 *   <li><b>圆形</b> — 中心点 + 半径判断</li>
 *   <li><b>多边形</b> — 射线法 (Ray Casting) 判断</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FenceEngineService {

    private final SafetyFenceMapper fenceMapper;
    private final FenceAlertEventMapper eventMapper;
    private final NotificationService notificationService;
    private final SsePushService ssePushService;
    private final ObjectMapper objectMapper;

    /** 目标在围栏内的状态跟踪: fenceId:targetId → true=inside */
    private final ConcurrentHashMap<String, Boolean> insideState = new ConcurrentHashMap<>();

    // ===================== 对外入口 =====================

    /**
     * 处理目标定位数据（外部定时任务或 MQTT 回调调用）
     *
     * @param projectId  项目ID
     * @param targetType person / device / vehicle
     * @param targetId   目标标识
     * @param targetName 目标名称（显示用）
     * @param lat        纬度
     * @param lng        经度
     */
    public void processLocation(Long projectId, String targetType, String targetId,
                                String targetName, BigDecimal lat, BigDecimal lng) {
        List<SafetyFence> fences = fenceMapper.selectActiveByProject(projectId);
        if (fences.isEmpty()) return;

        for (SafetyFence fence : fences) {
            boolean inside = isInside(fence, lat, lng);
            String stateKey = stateKey(fence.getId(), targetId);
            Boolean prevInside = insideState.get(stateKey);

            if (inside && !Boolean.TRUE.equals(prevInside)) {
                // 进入围栏
                fireEvent(fence, targetType, targetId, targetName, lat, lng, "enter");
                insideState.put(stateKey, true);
            } else if (!inside && Boolean.TRUE.equals(prevInside)) {
                // 离开围栏
                fireEvent(fence, targetType, targetId, targetName, lat, lng, "leave");
                insideState.put(stateKey, false);
            }
            // 状态未变则不产生事件
        }
    }

    /**
     * 批量处理定位数据
     */
    public void processLocationBatch(Long projectId, String targetType,
                                     List<Map<String, Object>> locations) {
        for (Map<String, Object> loc : locations) {
            String targetId = safeStr(loc.get("targetId"));
            String targetName = safeStr(loc.get("targetName"));
            BigDecimal lat = safeDecimal(loc.get("lat"));
            BigDecimal lng = safeDecimal(loc.get("lng"));
            if (targetId == null || lat == null || lng == null) continue;
            processLocation(projectId, targetType, targetId, targetName, lat, lng);
        }
    }

    /**
     * 清除目标的围栏状态（目标离开项目范围或解绑时调用）
     */
    public void clearTargetState(Long fenceId, String targetId) {
        insideState.remove(stateKey(fenceId, targetId));
    }

    /**
     * 清除某围栏的所有状态（围栏被删除或禁用时调用）
     */
    public void clearFenceState(Long fenceId) {
        insideState.keySet().removeIf(k -> k.startsWith(fenceId + ":"));
    }

    // ===================== 几何判断 =====================

    /**
     * 判断点是否在围栏内部
     */
    public boolean isInside(SafetyFence fence, BigDecimal lat, BigDecimal lng) {
        if (lat == null || lng == null) return false;
        double dlat = lat.doubleValue();
        double dlng = lng.doubleValue();

        if ("circle".equals(fence.getFenceType())) {
            return isInsideCircle(fence, dlat, dlng);
        } else {
            return isInsidePolygon(fence, dlat, dlng);
        }
    }

    /**
     * 圆形围栏判断：点到圆心的距离 ≤ 半径
     */
    private boolean isInsideCircle(SafetyFence fence, double lat, double lng) {
        if (fence.getCenterLat() == null || fence.getCenterLng() == null || fence.getRadiusM() == null) {
            return false;
        }
        double cx = fence.getCenterLng().doubleValue();
        double cy = fence.getCenterLat().doubleValue();
        double radius = fence.getRadiusM().doubleValue();

        // 近似: 1度 ≈ 111320m(经度) / 111320*cos(lat)(纬度)
        double latRad = Math.toRadians((cy + lat) / 2);
        double mPerDegLng = 111320.0 * Math.cos(latRad);
        double mPerDegLat = 111320.0;

        double dx = (lng - cx) * mPerDegLng;
        double dy = (lat - cy) * mPerDegLat;

        return (dx * dx + dy * dy) <= (radius * radius);
    }

    /**
     * 多边形围栏判断：射线法 (Ray Casting Algorithm)
     */
    private boolean isInsidePolygon(SafetyFence fence, double lat, double lng) {
        if (fence.getPolygonPoints() == null || fence.getPolygonPoints().isEmpty()) {
            return false;
        }
        try {
            List<List<Double>> points = objectMapper.readValue(
                    fence.getPolygonPoints(),
                    new TypeReference<List<List<Double>>>() {}
            );
            if (points.size() < 3) return false;

            int n = points.size();
            boolean inside = false;
            for (int i = 0, j = n - 1; i < n; j = i++) {
                double xi = points.get(i).get(1); // lat (index 1)
                double yi = points.get(i).get(0); // lng (index 0)
                double xj = points.get(j).get(1);
                double yj = points.get(j).get(0);

                boolean intersect = ((yi > lng) != (yj > lng))
                        && (lat < (xj - xi) * (lng - yi) / (yj - yi) + xi);
                if (intersect) inside = !inside;
            }
            return inside;
        } catch (Exception e) {
            log.warn("解析围栏多边形坐标失败: fenceId={}, points={}", fence.getId(), fence.getPolygonPoints(), e);
            return false;
        }
    }

    /**
     * 计算点到多边形围栏的最近距离（米）
     */
    public double minDistanceToFence(SafetyFence fence, BigDecimal lat, BigDecimal lng) {
        if (lat == null || lng == null) return Double.MAX_VALUE;
        double dlat = lat.doubleValue();
        double dlng = lng.doubleValue();

        if ("circle".equals(fence.getFenceType())) {
            if (fence.getCenterLat() == null || fence.getCenterLng() == null || fence.getRadiusM() == null)
                return Double.MAX_VALUE;
            double cx = fence.getCenterLng().doubleValue();
            double cy = fence.getCenterLat().doubleValue();
            double latRad = Math.toRadians((cy + dlat) / 2);
            double mPerDegLng = 111320.0 * Math.cos(latRad);
            double mPerDegLat = 111320.0;
            double dx = (dlng - cx) * mPerDegLng;
            double dy = (dlat - cy) * mPerDegLat;
            return Math.abs(Math.sqrt(dx * dx + dy * dy) - fence.getRadiusM().doubleValue());
        }

        // 多边形：计算点到每条边的最短距离
        try {
            List<List<Double>> pts = objectMapper.readValue(
                    fence.getPolygonPoints(),
                    new TypeReference<List<List<Double>>>() {}
            );
            if (pts.size() < 3) return Double.MAX_VALUE;

            double minDist = Double.MAX_VALUE;
            int n = pts.size();
            for (int i = 0, j = n - 1; i < n; j = i++) {
                double ax = pts.get(j).get(0), ay = pts.get(j).get(1);
                double bx = pts.get(i).get(0), by = pts.get(i).get(1);
                double dist = pointToSegmentDist(dlng, dlat, ax, ay, bx, by);
                if (dist < minDist) minDist = dist;
            }
            double latRad = Math.toRadians(dlat);
            double mPerDegLng = 111320.0 * Math.cos(latRad);
            return minDist * Math.min(mPerDegLng, 111320.0);
        } catch (Exception e) {
            log.warn("计算围栏距离失败", e);
            return Double.MAX_VALUE;
        }
    }

    /** 点到线段距离（经纬度度数） */
    private double pointToSegmentDist(double px, double py, double ax, double ay, double bx, double by) {
        double dx = bx - ax, dy = by - ay;
        if (dx == 0 && dy == 0) return Math.sqrt((px - ax) * (px - ax) + (py - ay) * (py - ay));
        double t = ((px - ax) * dx + (py - ay) * dy) / (dx * dx + dy * dy);
        t = Math.max(0, Math.min(1, t));
        double nearX = ax + t * dx, nearY = ay + t * dy;
        return Math.sqrt((px - nearX) * (px - nearX) + (py - nearY) * (py - nearY));
    }

    // ===================== 告警事件 =====================

    private void fireEvent(SafetyFence fence, String targetType, String targetId,
                           String targetName, BigDecimal lat, BigDecimal lng, String eventType) {
        FenceAlertEvent event = new FenceAlertEvent();
        event.setFenceId(fence.getId());
        event.setProjectId(fence.getProjectId());
        event.setEventType(eventType);
        event.setTargetType(targetType);
        event.setTargetId(targetId);
        event.setTargetName(targetName);
        event.setEventLat(lat);
        event.setEventLng(lng);
        event.setStatus("pending");
        event.setDescription(buildDescription(fence, targetName, eventType));
        eventMapper.insert(event);

        // SSE 实时推送
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("id", event.getId());
            payload.put("fenceId", fence.getId());
            payload.put("fenceName", fence.getFenceName());
            payload.put("eventType", eventType);
            payload.put("targetType", targetType);
            payload.put("targetId", targetId);
            payload.put("targetName", targetName);
            payload.put("lat", lat);
            payload.put("lng", lng);
            payload.put("description", event.getDescription());
            payload.put("alertLevel", fence.getAlertLevel());
            payload.put("createdAt", event.getCreatedAt() != null ? event.getCreatedAt().toString() : "");
            ssePushService.pushAll("fence_alert", payload);
        } catch (Exception e) {
            log.warn("围栏告警 SSE 推送失败", e);
        }

        // 站内通知（高等级告警）
        if ("high".equals(fence.getAlertLevel()) || "medium".equals(fence.getAlertLevel())) {
            // 使用 projectId 作为广播通知(projectId作为userId)
            try {
                notificationService.send(
                        0L,  // 系统通知
                        "围栏告警: " + fence.getFenceName(),
                        event.getDescription(),
                        "fence_alert",
                        event.getId(),
                        "high".equals(fence.getAlertLevel()) ? "warning" : "info"
                );
            } catch (Exception e) {
                log.warn("围栏通知创建失败", e);
            }
        }

        log.info("围栏告警: fence={}({}), target={}({}), event={} @ [{},{}]",
                fence.getFenceName(), fence.getId(), targetName, targetId, eventType, lat, lng);
    }

    private String buildDescription(SafetyFence fence, String targetName, String eventType) {
        if ("enter".equals(eventType)) {
            return targetName + " 进入围栏「" + fence.getFenceName() + "」范围";
        } else {
            return targetName + " 离开围栏「" + fence.getFenceName() + "」范围";
        }
    }

    // ===================== 工具 =====================

    private String stateKey(Long fenceId, String targetId) {
        return fenceId + ":" + targetId;
    }

    private String safeStr(Object v) {
        return v != null && !v.toString().isEmpty() ? v.toString() : null;
    }

    private BigDecimal safeDecimal(Object v) {
        if (v == null) return null;
        try { return new BigDecimal(v.toString()); } catch (NumberFormatException e) { return null; }
    }
}
