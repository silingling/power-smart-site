package com.powersmart.common.config;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * InfluxDB 时序数据库配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "influxdb")
public class InfluxDbConfig {

    private String url = "http://127.0.0.1:8086";
    private String token = "admin-token";
    private String org = "powersmart";
    private String bucket = "sensor_data";

    @Bean
    public InfluxDBClient influxDBClient() {
        return InfluxDBClientFactory.create(url, token.toCharArray(), org, bucket);
    }
}
