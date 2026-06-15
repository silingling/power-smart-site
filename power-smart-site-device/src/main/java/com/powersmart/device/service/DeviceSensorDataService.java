package com.powersmart.device.service;

import com.influxdb.client.InfluxDBClient;
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
 * <p>
 * 读取 InfluxDB 中存储的设备实时/历史工况数据。
 * 数据由 IoT 网关通过 MQTT 写入，格式：
 * measurement: device_sensor_data
 * tags: device_code, project_id, sensor_type
 * fields: value(float), unit(string)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceSensorDataService {

    private final InfluxDBClient influxDbClient;

    /**
     * 查询设备最新一条传感器数据
     */
    public Map<String, Object> getLatestSensorData(String deviceCode) {
        String flux = String.format(
                "from(bucket: \"sensor_data\") " +
                        "|> range(start: -1h) " +
                        "|> filter(fn: (r) => r[\"device_code\"] == \"%s\") " +
                        "|> last()",
                deviceCode);

        List<FluxTable> tables = influxDbClient.getQueryApi().query(flux);
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
     */
    public Map<String, List<Map<String, Object>>> getHistoryTrend(String deviceCode, int hours) {
        String flux = String.format(
                "from(bucket: \"sensor_data\") " +
                        "|> range(start: -%dh) " +
                        "|> filter(fn: (r) => r[\"device_code\"] == \"%s\") " +
                        "|> aggregateWindow(every: 5m, fn: mean)",
                hours, deviceCode);

        List<FluxTable> tables = influxDbClient.getQueryApi().query(flux);
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
        // 通过 InfluxDB WriteApi 写入时序数据
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
}
