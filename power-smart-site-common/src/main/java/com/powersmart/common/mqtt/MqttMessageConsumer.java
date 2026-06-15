package com.powersmart.common.mqtt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.Message;

/**
 * MQTT 消息消费——接收 IoT 设备上报数据
 *
 * 订阅主题：
 * - device/+/data        → 设备传感器数据
 * - location/+/update    → 人员定位更新
 * - alarm/+/trigger      → 硬件告警触发
 *
 * 可通过 mqtt.enabled=false 关闭 MQTT 功能（单元测试或离线环境）
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "mqtt.enabled", havingValue = "true", matchIfMissing = true)
public class MqttMessageConsumer {

    private final ObjectMapper objectMapper;

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

    private void handleDeviceData(JsonNode root) {
        String deviceCode = root.path("deviceCode").asText();
        String projectId = root.path("projectId").asText();

        JsonNode sensors = root.path("sensors");
        if (sensors.isArray()) {
            for (JsonNode sensor : sensors) {
                String type = sensor.path("type").asText();
                double value = sensor.path("value").asDouble();
                String unit = sensor.path("unit").asText();
                log.info("设备传感器数据 deviceCode={}, type={}, value={}{}",
                        deviceCode, type, value, unit);
            }
        }
    }

    private void handleLocationUpdate(JsonNode root) {
        String workerId = root.path("workerId").asText();
        String projectId = root.path("projectId").asText();
        double latitude = root.path("latitude").asDouble();
        double longitude = root.path("longitude").asDouble();
        double altitude = root.path("altitude").asDouble();
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
