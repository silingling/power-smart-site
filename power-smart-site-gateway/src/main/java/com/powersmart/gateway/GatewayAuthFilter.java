package com.powersmart.gateway;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * 网关级 JWT 认证过滤器
 *
 * 所有经过网关的 /build/** 和 /api/** 请求必须携带有效 Admin-Token。
 * 白名单路径直接放行。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GatewayAuthFilter implements GlobalFilter, Ordered {

    private static final Set<String> WHITELIST = Set.of(
            "/login",
            "/logout",
            "/adminCommon/",
            "/adminUser/querySystemStatus",
            "/adminUser/initUser"
    );

    private final ObjectMapper objectMapper;

    @Value("${gateway.auth.enabled:true}")
    private boolean authEnabled;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!authEnabled) {
            return chain.filter(exchange);
        }

        String path = exchange.getRequest().getURI().getPath();

        // 白名单放行
        if (isWhitelisted(path)) {
            return chain.filter(exchange);
        }

        // 只拦截 /build/ 路径（前端 API 请求）
        if (!path.startsWith("/build/") && !path.startsWith("/api/")) {
            return chain.filter(exchange);
        }

        String token = exchange.getRequest().getHeaders().getFirst("Admin-Token");
        String ip = exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown";

        if (token == null || token.isEmpty()) {
            log.warn("网关认证失败: path={}, ip={}, 缺少 Admin-Token", path, ip);
            return unauthorized(exchange.getResponse(), "登录已过期，请重新登录");
        }

        // 注意：网关不解析 JWT payload（让下游服务验证）
        // 仅检查 token 是否存在，并将 token 向下游传递
        return chain.filter(exchange);
    }

    private Mono<Void> unauthorized(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        try {
            String body = objectMapper.writeValueAsString(
                    java.util.Map.of("code", 401, "msg", message, "data", null));
            DataBuffer buffer = response.bufferFactory()
                    .wrap(body.getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            return response.setComplete();
        }
    }

    private boolean isWhitelisted(String path) {
        for (String w : WHITELIST) {
            if (path.equals(w) || path.startsWith(w)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        return -100; // 在路由之前执行
    }
}
