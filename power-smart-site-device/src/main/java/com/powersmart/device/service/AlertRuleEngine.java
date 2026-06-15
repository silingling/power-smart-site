package com.powersmart.device.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.powersmart.common.push.SsePushService;
import com.powersmart.device.entity.AlertRule;
import com.powersmart.device.entity.DeviceAlarm;
import com.powersmart.device.entity.SystemNotification;
import com.powersmart.device.mapper.AlertRuleMapper;
import com.powersmart.device.mapper.DeviceAlarmMapper;
import com.powersmart.device.mapper.SystemNotificationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 告警规则引擎 — 基于 DB 可配置规则
 *
 * <p>取代 DeviceAlarmService 中硬编码的阈值判断。
 * 支持：DB 规则 → 匹配传感器 → 限幅防抖 → 生成告警 → SSE 推送</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertRuleEngine {

    private final AlertRuleMapper ruleMapper;
    private final DeviceAlarmMapper alarmMapper;
    private final SystemNotificationMapper notificationMapper;
    private final SsePushService ssePushService;
    private final ObjectMapper objectMapper;

    /** 防抖缓存：sensorType:deviceId → (lastAlertValue, lastAlertTime) */
    private final ConcurrentHashMap<String, DebounceEntry> debounceCache = new ConcurrentHashMap<>();

    /**
     * 评估单个传感器值是否触发告警
     *
     * @param deviceId    设备 ID
     * @param deviceType  设备类型（塔吊/电焊机/...）
     * @param sensorType  传感器类型（load/tilt/pm25/...）
     * @param value       当前值
     */
    public void evaluate(Long deviceId, String deviceType, String sensorType, double value) {
        List<AlertRule> rules = ruleMapper.selectRulesByDeviceType(deviceType);
        for (AlertRule rule : rules) {
            if (!rule.getSensorType().equals(sensorType)) continue;

            double threshold = value;
            String level = "warning";
            boolean exceeded = false;

            // 匹配比较符
            BigDecimal bdValue = BigDecimal.valueOf(value);
            if ("gt".equals(rule.getOperator())) {
                if (rule.getCriticalThreshold() != null && bdValue.compareTo(rule.getCriticalThreshold()) > 0) {
                    threshold = rule.getCriticalThreshold().doubleValue();
                    level = "critical";
                    exceeded = true;
                } else if (rule.getWarningThreshold() != null && bdValue.compareTo(rule.getWarningThreshold()) > 0) {
                    threshold = rule.getWarningThreshold().doubleValue();
                    level = "warning";
                    exceeded = true;
                }
            } else if ("lt".equals(rule.getOperator())) {
                if (rule.getCriticalThreshold() != null && bdValue.compareTo(rule.getCriticalThreshold()) < 0) {
                    threshold = rule.getCriticalThreshold().doubleValue();
                    level = "critical";
                    exceeded = true;
                } else if (rule.getWarningThreshold() != null && bdValue.compareTo(rule.getWarningThreshold()) < 0) {
                    threshold = rule.getWarningThreshold().doubleValue();
                    level = "warning";
                    exceeded = true;
                }
            } else if ("gte".equals(rule.getOperator())) {
                if (rule.getCriticalThreshold() != null && bdValue.compareTo(rule.getCriticalThreshold()) >= 0) {
                    threshold = rule.getCriticalThreshold().doubleValue();
                    level = "critical";
                    exceeded = true;
                } else if (rule.getWarningThreshold() != null && bdValue.compareTo(rule.getWarningThreshold()) >= 0) {
                    threshold = rule.getWarningThreshold().doubleValue();
                    level = "warning";
                    exceeded = true;
                }
            }

            if (!exceeded) {
                // 未超限：清除防抖
                debounceCache.remove(debounceKey(deviceId, sensorType));
                continue;
            }

            // 防抖检查
            if (rule.getDurationSeconds() != null && rule.getDurationSeconds() > 0) {
                String key = debounceKey(deviceId, sensorType);
                DebounceEntry entry = debounceCache.get(key);
                long now = System.currentTimeMillis();

                if (entry != null && Math.abs(value - entry.value) < 0.01) {
                    // 持续超限中，检查是否达到防抖时长
                    if (now - entry.firstAlertTime < rule.getDurationSeconds() * 1000L) {
                        continue; // 防抖时间未到
                    }
                } else {
                    // 首次超限或值有变化，记录开始时间
                    debounceCache.put(key, new DebounceEntry(value, now));
                    continue;
                }
            }

            // 触发告警
            fireAlert(deviceId, deviceType, sensorType, level, value, threshold, rule.getRuleName());
        }
    }

    private void fireAlert(Long deviceId, String deviceType, String sensorType,
                           String level, double value, double threshold, String ruleName) {
        // 1. 写入 device_alarm
        DeviceAlarm alarm = new DeviceAlarm();
        alarm.setDeviceId(deviceId);
        alarm.setAlarmType(sensorType);
        alarm.setAlarmLevel(level);
        alarm.setAlarmValue(value);
        alarm.setThresholdValue(threshold);
        alarm.setDescription(String.format("[%s] %s: %.2f (阈值: %.2f)", ruleName, sensorType, value, threshold));
        alarm.setStatus(0);
        alarmMapper.insert(alarm);

        log.warn("告警触发 deviceId={}, type={}, level={}, value={}, threshold={}",
                deviceId, sensorType, level, value, threshold);

        // 2. 创建系统通知
        SystemNotification notif = new SystemNotification();
        notif.setUserId(0L);       // 0 = 广播给所有管理员
        notif.setTitle("设备告警: " + (deviceType != null ? deviceType : "未知设备"));
        notif.setContent(alarm.getDescription());
        notif.setBizType("device_alarm");
        notif.setBizId(alarm.getId());
        notif.setLevel(level);
        notificationMapper.insert(notif);

        // 3. SSE 实时推送
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("type", "device_alarm");
            payload.put("alarmId", alarm.getId());
            payload.put("deviceId", deviceId);
            payload.put("deviceType", deviceType);
            payload.put("sensorType", sensorType);
            payload.put("level", level);
            payload.put("value", value);
            payload.put("threshold", threshold);
            payload.put("description", alarm.getDescription());
            payload.put("timestamp", System.currentTimeMillis());

            String json = objectMapper.writeValueAsString(payload);
            ssePushService.pushAll("alert", json);
        } catch (Exception e) {
            log.warn("SSE 推送告警失败", e);
        }
    }

    private String debounceKey(Long deviceId, String sensorType) {
        return sensorType + ":" + deviceId;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    private static class DebounceEntry {
        private double value;
        private long firstAlertTime;
    }
}
