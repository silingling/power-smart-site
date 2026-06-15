package com.powersmart.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * 网关请求日志过滤器
 *
 * 为每个经过网关的请求注入 X-Trace-Id，
 * 记录请求路径、方法、客户端 IP 和耗时。
 */
@Slf4j
@Component
public class GatewayLoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethodValue();
        String ip = request.getRemoteAddress() != null
                ? request.getRemoteAddress().getAddress().getHostAddress() : "unknown";

        // 注入追踪 ID
        String traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-Trace-Id", traceId)
                .build();
        ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();

        long start = System.currentTimeMillis();

        return chain.filter(mutatedExchange).then(Mono.fromRunnable(() -> {
            long elapsed = System.currentTimeMillis() - start;
            int status = mutatedExchange.getResponse().getStatusCode() != null
                    ? mutatedExchange.getResponse().getStatusCode().value() : 0;

            // 慢请求告警（> 3s）
            if (elapsed > 3000) {
                log.warn("[慢请求] {} {} → {} ({}ms) ip={} traceId={}",
                        method, path, status, elapsed, ip, traceId);
            } else if (log.isDebugEnabled()) {
                log.debug("[网关] {} {} → {} ({}ms) ip={} traceId={}",
                        method, path, status, elapsed, ip, traceId);
            }
        }));
    }

    @Override
    public int getOrder() {
        return -200; // 在 AuthFilter 之前执行
    }
}
