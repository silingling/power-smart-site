package com.powersmart.common.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.powersmart.common.entity.Result;
import com.powersmart.common.util.RedisUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * JWT 认证过滤器
 *
 * 校验每个请求的 Admin-Token header。
 * 白名单路径直接放行（登录/注册/静态资源等）。
 *
 * 安全说明：
 * - 所有 /api/** 和 /build/** 请求必须携带有效 token
 * - token 过期返回 401，由前端跳转登录页
 * - 白名单路径之外的任何请求都会触发认证检查
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class AuthFilter {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    /** 白名单：无需认证即可访问的路径 */
    private static final Set<String> WHITELIST = Set.of(
            "/login",
            "/logout",
            "/adminCommon/getBType",
            "/adminUser/querySystemStatus",
            "/adminUser/initUser",
            "/build/dashboard/",
            "/api/v1/dashboard/",
            "/api/v1/sse/subscribe",
            "/swagger-ui",
            "/v3/api-docs",
            "/doc.html"
    );

    @Bean
    @Order(1)
    public FilterRegistrationBean<Filter> jwtFilter() {
        FilterRegistrationBean<Filter> reg = new FilterRegistrationBean<>();
        reg.setFilter((request, response, chain) -> {
            HttpServletRequest httpReq = (HttpServletRequest) request;
            HttpServletResponse httpResp = (HttpServletResponse) response;
            String path = httpReq.getRequestURI();

            // 白名单放行
            if (isWhitelisted(path)) {
                chain.doFilter(request, response);
                return;
            }

            // 只拦截 /api/ 和 /build/ 路径
            if (!path.startsWith("/api/") && !path.startsWith("/build/")) {
                chain.doFilter(request, response);
                return;
            }

            String token = httpReq.getHeader("Admin-Token");
            if (token == null || !jwtUtil.validateToken(token) || RedisUtil.isBlacklisted(token)) {
                log.warn("认证失败: path={}, ip={}", path, httpReq.getRemoteAddr());
                httpResp.setContentType("application/json;charset=UTF-8");
                httpResp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                objectMapper.writeValue(httpResp.getWriter(),
                        Result.fail(401, "登录已过期，请重新登录"));
                return;
            }

            // 设置安全上下文
            Long userId = jwtUtil.getUserIdFromToken(token);
            String username = jwtUtil.getUsernameFromToken(token);
            List<String> permissions = jwtUtil.getPermissionsFromToken(token);
            SecurityContext.setCurrentUser(userId, username, permissions);

            try {
                chain.doFilter(request, response);
            } finally {
                SecurityContext.clear();
            }
        });

        reg.addUrlPatterns("/*");
        reg.setOrder(1);
        return reg;
    }

    private boolean isWhitelisted(String path) {
        for (String w : WHITELIST) {
            if (path.equals(w) || path.startsWith(w)) {
                return true;
            }
        }
        return false;
    }
}
