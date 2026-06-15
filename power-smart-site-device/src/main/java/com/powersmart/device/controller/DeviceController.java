package com.powersmart.device.controller;

import com.powersmart.common.entity.Result;
import com.powersmart.device.entity.Device;
import com.powersmart.device.entity.DeviceAlarm;
import com.powersmart.device.service.DeviceAlarmService;
import com.powersmart.device.service.DeviceSensorDataService;
import com.powersmart.device.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;
    private final DeviceSensorDataService sensorDataService;
    private final DeviceAlarmService alarmService;

    // ====== 设备台账管理 ======

    @PostMapping
    public Result<Device> create(@RequestBody Device device) {
        deviceService.save(device);
        return Result.ok(device);
    }

    @GetMapping
    public Result<List<Device>> list(
            @RequestParam Long projectId,
            @RequestParam(required = false) String deviceType,
            @RequestParam(required = false) Integer status) {
        return Result.ok(deviceService.getByProject(projectId, deviceType, status));
    }

    @GetMapping("/{id}")
    public Result<Device> getById(@PathVariable Long id) {
        return Result.ok(deviceService.getById(id));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Device device) {
        device.setId(id);
        deviceService.updateById(device);
        return Result.ok();
    }

    // ====== 传感器数据 ======

    @GetMapping("/{id}/sensor-data")
    public Result<Map<String, Object>> getSensorData(@PathVariable Long id,
                                                     @RequestParam(defaultValue = "latest") String mode,
                                                     @RequestParam(defaultValue = "1") int hours) {
        Device device = deviceService.getById(id);
        if (device == null) return Result.fail("设备不存在");

        if ("trend".equals(mode)) {
            return Result.ok(Map.of("trend", sensorDataService.getHistoryTrend(device.getDeviceCode(), hours)));
        }
        return Result.ok(Map.of("latest", sensorDataService.getLatestSensorData(device.getDeviceCode())));
    }

    @PostMapping("/{id}/sensor-data/write")
    public Result<Void> writeSensorData(
            @PathVariable Long id,
            @RequestParam String sensorType,
            @RequestParam double value,
            @RequestParam(defaultValue = "") String unit) {
        Device device = deviceService.getById(id);
        if (device == null) return Result.fail("设备不存在");
        sensorDataService.writeSensorData(device.getDeviceCode(),
                String.valueOf(device.getProjectId()), sensorType, value, unit);
        return Result.ok();
    }

    // ====== 设备告警 ======

    @GetMapping("/{id}/alarms")
    public Result<List<DeviceAlarm>> getAlarms(@PathVariable Long id,
                                               @RequestParam(required = false) Integer status) {
        if (status != null && status == 0) {
            return Result.ok(alarmService.getUnhandledAlarms(id));
        }
        return Result.ok(alarmService.getUnhandledAlarms(id));
    }

    @PostMapping("/{id}/check-alarms")
    public Result<Void> checkAlarms(@PathVariable Long id) {
        alarmService.checkDeviceThresholds(id);
        return Result.ok();
    }

    @PutMapping("/alarms/{alarmId}/handle")
    public Result<Void> handleAlarm(@PathVariable Long alarmId,
                                    @RequestParam Long userId,
                                    @RequestParam(defaultValue = "handled") String action) {
        alarmService.handleAlarm(alarmId, userId, action);
        return Result.ok();
    }
}
