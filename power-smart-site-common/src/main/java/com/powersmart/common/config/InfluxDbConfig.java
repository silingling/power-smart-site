package com.powersmart.common.config;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * InfluxDB 时序数据库配置
 *
 * 安全说明：token 从配置中心(Nacos)或环境变量读取，
 * 禁止硬编码在源码中。
 * 部署时通过 SPRING_CLOUD_NACOS_CONFIG 或环境变量 INFLUXDB_TOKEN 注入。
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "influxdb")
public class InfluxDbConfig {

    /** 默认值仅供本地开发使用，生产环境必须通过配置中心覆盖 */
    private String url = "http://127.0.0.1:8086";
    private String token = "${INFLUXDB_TOKEN:local-dev-token}";
    private String org = "powersmart";
    private String bucket = "sensor_data";

    @Bean
    public InfluxDBClient influxDBClient() {
        return InfluxDBClientFactory.create(url, token.toCharArray(), org, bucket);
    }
}
