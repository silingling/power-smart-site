package com.powersmart.hazard;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.powersmart"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.powersmart.api")
@EnableScheduling
@MapperScan("com.powersmart")
public class HazardApplication {
    public static void main(String[] args) {
        SpringApplication.run(HazardApplication.class, args);
    }
}
