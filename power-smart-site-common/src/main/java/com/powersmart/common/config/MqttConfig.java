package com.powersmart.common.config;

import lombok.Data;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;

/**
 * MQTT 物联网接入配置
 *
 * 安全说明：
 * - brokerUrl / username / password 从配置中心(Nacos)读取
 * - 本地开发默认值不可用于生产
 * - 生产环境通过 NACOS 配置或环境变量 MQTT_PASSWORD 覆盖
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "mqtt")
public class MqttConfig {

    private String brokerUrl = "tcp://127.0.0.1:1883";
    private String clientId = "power-smart-site";
    private String username = "powersmart";
    private String password = "${MQTT_PASSWORD:powersmart123}";
    private String[] inboundTopics = {"device/+/data", "location/+/update", "alarm/+/trigger"};

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[]{brokerUrl});
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        options.setCleanSession(true);
        options.setKeepAliveInterval(30);
        options.setAutomaticReconnect(true);
        options.setConnectionTimeout(10);

        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        factory.setConnectionOptions(options);
        return factory;
    }
}
