package com.powersmart.device.service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 设备传感器时序数据查询服务
 *
 * 安全说明：使用参数化查询（Flux 变量绑定）防止 Flux 注入。
 * 严禁直接拼接用户输入到 Flux 字符串中。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceSensorDataService {

    private final InfluxDBClient influxDbClient;

    /**
     * 查询设备最新一条传感器数据
     * 使用参数化查询防止 Flux 注入
     */
    public Map<String, Object> getLatestSensorData(String deviceCode) {
        // 校验：只允许字母数字和连字符/下划线
        validateSafeInput(deviceCode, "deviceCode");

        String flux = "from(bucket: \"sensor_data\") "
                + "|> range(start: -1h) "
                + "|> filter(fn: (r) => r[\"device_code\"] == v.deviceCode) "
                + "|> last()";

        Map<String, Object> variables = Map.of("deviceCode", deviceCode);
        QueryApi queryApi = influxDbClient.getQueryApi();
        List<FluxTable> tables = queryApi.query(flux, variables);

        Map<String, Object> result = new LinkedHashMap<>();

        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                String sensorType = (String) record.getValueByKey("sensor_type");
                Double value = (Double) record.getValueByKey("_value");
                String unit = (String) record.getValueByKey("unit");
                Instant time = record.getTime();

                if (sensorType != null) {
                    Map<String, Object> sensorData = new HashMap<>();
                    sensorData.put("value", value);
                    sensorData.put("unit", unit);
                    sensorData.put("time", time);
                    result.put(sensorType, sensorData);
                }
            }
        }
        return result;
    }

    /**
     * 查询设备传感器历史趋势数据（最近 N 小时）
     * 使用参数化查询防止 Flux 注入
     */
    public Map<String, List<Map<String, Object>>> getHistoryTrend(String deviceCode, int hours) {
        validateSafeInput(deviceCode, "deviceCode");

        String flux = "from(bucket: \"sensor_data\") "
                + "|> range(start: -v.hours) "
                + "|> filter(fn: (r) => r[\"device_code\"] == v.deviceCode) "
                + "|> aggregateWindow(every: 5m, fn: mean)";

        Map<String, Object> variables = new HashMap<>();
        variables.put("deviceCode", deviceCode);
        variables.put("hours", hours + "h");

        List<FluxTable> tables = influxDbClient.getQueryApi().query(flux, variables);
        Map<String, List<Map<String, Object>>> trend = new LinkedHashMap<>();

        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                String sensorType = (String) record.getValueByKey("sensor_type");
                Double value = (Double) record.getValueByKey("_value");
                Instant time = record.getTime();

                trend.computeIfAbsent(sensorType, k -> new ArrayList<>());
                Map<String, Object> point = new HashMap<>();
                point.put("time", time.toString());
                point.put("value", value);
                trend.get(sensorType).add(point);
            }
        }
        return trend;
    }

    /**
     * 写入传感器数据（供 IoT 网关 MQTT 消费端调用）
     */
    public void writeSensorData(String deviceCode, String projectId,
                                String sensorType, double value, String unit) {
        validateSafeInput(deviceCode, "deviceCode");

        var point = com.influxdb.client.domain.Point
                .measurement("device_sensor_data")
                .addTag("device_code", deviceCode)
                .addTag("project_id", projectId)
                .addTag("sensor_type", sensorType)
                .addField("value", value)
                .addField("unit", unit)
                .time(System.currentTimeMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);

        try (var writeApi = influxDbClient.getWriteApi()) {
            writeApi.writePoint(point);
        } catch (Exception e) {
            log.error("写入传感器数据失败 deviceCode={}, sensorType={}", deviceCode, sensorType, e);
        }
    }

    /**
     * 写入传感器数据（通用字段 Map 版本）
     * 供 MqttMessageConsumer 使用
     */
    public void writeSensorData(String deviceCode, String sensorType, Map<String, Object> fields) {
        validateSafeInput(deviceCode, "deviceCode");

        var point = com.influxdb.client.domain.Point
                .measurement("device_sensor_data")
                .addTag("device_code", deviceCode)
                .addTag("sensor_type", sensorType)
                .time(System.currentTimeMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);

        // 动态添加字段
        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            Object val = entry.getValue();
            if (val instanceof Number) {
                point.addField(entry.getKey(), ((Number) val).doubleValue());
            } else if (val instanceof String) {
                point.addField(entry.getKey(), (String) val);
            } else if (val instanceof Boolean) {
                point.addField(entry.getKey(), (Boolean) val);
            }
        }

        try (var writeApi = influxDbClient.getWriteApi()) {
            writeApi.writePoint(point);
        } catch (Exception e) {
            log.error("写入传感器数据失败 deviceCode={}, sensorType={}", deviceCode, sensorType, e);
        }
    }

    /**
     * 校验输入: 只允许字母数字、连字符、下划线、冒号、点
     * 防止 Flux 注入
     */
    private void validateSafeInput(String input, String fieldName) {
        if (input == null || !input.matches("^[a-zA-Z0-9_\\-.:]+$")) {
            throw new IllegalArgumentException(
                    String.format("非法 %s 参数: %s", fieldName, input));
        }
    }
}
