package com.powersmart.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;

@SpringBootApplication(scanBasePackages = {"com.powersmart", "com.powersmart.gateway"})
@EnableDiscoveryClient
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

    /**
     * 限流 key 解析器（按客户端 IP）
     */
    @Bean
    public KeyResolver remoteAddrKeyResolver() {
        return exchange -> {
            String ip = Objects.requireNonNull(
                            exchange.getRequest().getRemoteAddress())
                    .getAddress().getHostAddress();
            return Mono.just(ip);
        };
    }

    public interface KeyResolver {
        Mono<String> resolve(ServerWebExchange exchange);
    }
}
