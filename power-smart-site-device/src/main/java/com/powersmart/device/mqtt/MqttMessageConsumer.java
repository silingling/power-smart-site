package com.powersmart.device.mqtt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.powersmart.device.service.AlertRuleEngine;
import com.powersmart.device.service.DeviceSensorDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.Message;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * MQTT 消息消费（设备模块）— 接收 IoT 设备上报数据
 *
 * <p>订阅主题：</p>
 * <ul>
 *   <li>device/+/data        → 传感器数据 → InfluxDB + 告警规则引擎 + SSE 推送</li>
 *   <li>location/+/update    → 人员定位 → InfluxDB</li>
 *   <li>alarm/+/trigger      → 硬件告警 → 直接入库</li>
 * </ul>
 *
 * <p>通过 mqtt.enabled=false 可关闭 MQTT。</p>
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "mqtt.enabled", havingValue = "true", matchIfMissing = true)
public class MqttMessageConsumer {

    private final ObjectMapper objectMapper;
    private final DeviceSensorDataService sensorDataService;
    private final AlertRuleEngine alertRuleEngine;

    @Bean
    public MqttPahoMessageDrivenChannelAdapter inboundAdapter(MqttPahoClientFactory factory) {
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter("power-smart-consumer", factory,
                        "device/+/data", "location/+/update", "alarm/+/trigger");
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(1);
        return adapter;
    }

    @Bean
    public IntegrationFlow mqttInbound(MqttPahoMessageDrivenChannelAdapter adapter) {
        return IntegrationFlows.from(adapter)
                .handle(this::handleMqttMessage)
                .get();
    }

    private void handleMqttMessage(Message<?> message) {
        try {
            String topic = (String) message.getHeaders().get("mqtt_topic");
            String payload = new String((byte[]) message.getPayload());

            if (log.isDebugEnabled()) {
                log.debug("MQTT 收到 topic={}, payload={}", topic, payload);
            }

            if (topic == null || payload == null) return;

            JsonNode root = objectMapper.readTree(payload);

            if (topic.startsWith("device/")) {
                handleDeviceData(root);
            } else if (topic.startsWith("location/")) {
                handleLocationUpdate(root);
            } else if (topic.startsWith("alarm/")) {
                handleHardwareAlarm(root);
            }
        } catch (Exception e) {
            log.error("MQTT 消息处理失败", e);
        }
    }

    /**
     * 设备传感器数据 → InfluxDB 写入 + 告警规则引擎
     */
    private void handleDeviceData(JsonNode root) {
        String deviceCode = root.path("deviceCode").asText();
        String deviceType = root.path("deviceType").asText("");
        Long deviceId = root.path("deviceId").asLong();
        Long projectId = root.path("projectId").asLong();

        JsonNode sensors = root.path("sensors");
        if (!sensors.isArray() || sensors.isEmpty()) return;

        for (JsonNode sensor : sensors) {
            String type = sensor.path("type").asText();
            double value = sensor.path("value").asDouble();
            String unit = sensor.path("unit").asText("");

            // 1. 写入 InfluxDB 时序数据
            Map<String, Object> fields = new LinkedHashMap<>();
            fields.put("value", value);
            if (!unit.isEmpty()) fields.put("unit", unit);
            fields.put("device_code", deviceCode);
            fields.put("device_type", deviceType);
            fields.put("project_id", String.valueOf(projectId));

            try {
                sensorDataService.writeSensorData(deviceCode, type, fields);
            } catch (Exception e) {
                log.warn("InfluxDB 写入失败: deviceCode={}, sensor={}", deviceCode, type);
            }

            // 2. 触发告警规则检查
            if (deviceId != null && deviceId > 0) {
                try {
                    alertRuleEngine.evaluate(deviceId, deviceType, type, value);
                } catch (Exception e) {
                    log.warn("告警规则评估失败: deviceId={}, sensor={}", deviceId, type);
                }
            }

            log.debug("设备数据处理完成: code={}, sensor={}, value={}{}", deviceCode, type, value, unit);
        }
    }

    private void handleLocationUpdate(JsonNode root) {
        String workerId = root.path("workerId").asText();
        Long projectId = root.path("projectId").asLong();
        double latitude = root.path("latitude").asDouble();
        double longitude = root.path("longitude").asDouble();
        double altitude = root.path("altitude").asDouble();

        // 写入 InfluxDB
        try {
            Map<String, Object> fields = new LinkedHashMap<>();
            fields.put("latitude", latitude);
            fields.put("longitude", longitude);
            fields.put("altitude", altitude);
            fields.put("project_id", String.valueOf(projectId));
            sensorDataService.writeSensorData("loc_" + workerId, "location", fields);
        } catch (Exception e) {
            log.warn("定位数据写入失败: workerId={}", workerId);
        }

        log.debug("人员定位 workerId={}, pos=({}, {}, {})", workerId, latitude, longitude, altitude);
    }

    private void handleHardwareAlarm(JsonNode root) {
        String deviceCode = root.path("deviceCode").asText();
        String alarmType = root.path("alarmType").asText();
        String alarmLevel = root.path("level").asText();
        String description = root.path("description").asText();
        log.warn("硬件告警 deviceCode={}, type={}, level={}, desc={}",
                deviceCode, alarmType, alarmLevel, description);
    }
}
