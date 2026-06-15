package com.powersmart.device.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powersmart.common.constant.ApiConstant;
import com.powersmart.common.exception.BusinessException;
import com.powersmart.device.entity.Device;
import com.powersmart.device.entity.DeviceAlarm;
import com.powersmart.device.mapper.DeviceAlarmMapper;
import com.powersmart.device.mapper.DeviceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 设备告警处理服务
 * 实现：阈值比对 → 告警生成 → 趋势预判
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceAlarmService {

    private final DeviceAlarmMapper alarmMapper;
    private final DeviceMapper deviceMapper;
    private final DeviceSensorDataService sensorDataService;

    /**
     * 检查单台设备的所有传感器是否有超限，自动生成告警
     */
    public void checkDeviceThresholds(Long deviceId) {
        Device device = deviceMapper.selectById(deviceId);
        if (device == null) return;

        Map<String, Object> latestData = sensorDataService.getLatestSensorData(device.getDeviceCode());
        if (latestData.isEmpty()) return;

        for (Map.Entry<String, Object> entry : latestData.entrySet()) {
            String sensorType = entry.getKey();
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) entry.getValue();
            Double value = (Double) data.get("value");
            if (value == null) continue;

            // 针对不同传感器类型检查阈值
            ThresholdResult threshold = checkThreshold(device.getDeviceType(), sensorType, value);
            if (threshold.isExceeded()) {
                createAlarm(deviceId, sensorType, threshold.getLevel(), value, threshold.getThreshold());
            }
        }
    }

    /**
     * 获取设备未处理的告警列表
     */
    public List<DeviceAlarm> getUnhandledAlarms(Long deviceId) {
        return alarmMapper.selectList(new LambdaQueryWrapper<DeviceAlarm>()
                .eq(DeviceAlarm::getDeviceId, deviceId)
                .eq(DeviceAlarm::getStatus, 0)
                .orderByDesc(DeviceAlarm::getCreatedAt));
    }

    /**
     * 处理告警
     */
    public void handleAlarm(Long alarmId, Long userId, String action) {
        DeviceAlarm alarm = alarmMapper.selectById(alarmId);
        if (alarm == null) throw new BusinessException("告警记录不存在");

        alarm.setStatus("handled".equals(action) ? 1 : 2);
        alarm.setHandledBy(userId);
        alarm.setHandledTime(LocalDateTime.now());
        alarmMapper.updateById(alarm);
    }

    private void createAlarm(Long deviceId, String sensorType, String level,
                             Double alarmValue, Double threshold) {
        DeviceAlarm alarm = new DeviceAlarm();
        alarm.setDeviceId(deviceId);
        alarm.setAlarmType(sensorType);
        alarm.setAlarmLevel(level);
        alarm.setAlarmValue(alarmValue);
        alarm.setThresholdValue(threshold);
        alarm.setDescription(String.format("%s 超限: %.2f (阈值: %.2f)", sensorType, alarmValue, threshold));
        alarm.setStatus(0);
        alarmMapper.insert(alarm);
        log.warn("设备告警 deviceId={}, type={}, value={}, threshold={}", deviceId, sensorType, alarmValue, threshold);
    }

    /**
     * 阈值检查——按设备类型和传感器类型配置
     * 可扩展为从数据库读取阈值配置
     */
    private ThresholdResult checkThreshold(String deviceType, String sensorType, double value) {
        // 塔吊安全阈值
        if ("塔吊".equals(deviceType) || "汽车吊".equals(deviceType)) {
            switch (sensorType) {
                case "load":        // 起重量
                    if (value > 90) return ThresholdResult.critical(value, 90);
                    if (value > 80) return ThresholdResult.warning(value, 80);
                    break;
                case "tilt":        // 倾角
                    if (value > 3.0) return ThresholdResult.critical(value, 3.0);
                    if (value > 2.0) return ThresholdResult.warning(value, 2.0);
                    break;
                case "wind_speed":  // 风速
                    if (value > 13.8) return ThresholdResult.critical(value, 13.8);
                    if (value > 10.7) return ThresholdResult.warning(value, 10.7);
                    break;
            }
        }
        // 电焊机安全阈值
        if ("电焊机".equals(deviceType)) {
            switch (sensorType) {
                case "temperature":
                    if (value > 85) return ThresholdResult.critical(value, 85);
                    if (value > 65) return ThresholdResult.warning(value, 65);
                    break;
                case "current":
                    if (value > 500) return ThresholdResult.critical(value, 500);
                    break;
            }
        }
        return ThresholdResult.normal();
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ThresholdResult {
        private boolean exceeded;
        private String level;
        private double value;
        private double threshold;

        static ThresholdResult normal() {
            return new ThresholdResult(false, "normal", 0, 0);
        }

        static ThresholdResult warning(double value, double threshold) {
            return new ThresholdResult(true, "warning", value, threshold);
        }

        static ThresholdResult critical(double value, double threshold) {
            return new ThresholdResult(true, "critical", value, threshold);
        }
    }
}
