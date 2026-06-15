package com.powersmart.common.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * JWT 工具类
 *
 * 安全说明：
 * - jwt.secret 必须通过配置中心或环境变量注入，生产环境不低于 256 位
 * - 默认密钥仅供本地开发，不可用于生产
 */
@Component
public class JwtUtil {

    private static final long TOKEN_VALIDITY_MS = 24 * 60 * 60 * 1000L; // 24小时

    private final SecretKey secretKey;

    public JwtUtil(@Value("${jwt.secret:DefaultDevSecretKeyForLocalOnly_Min256Bits!!}") String secret) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        // 确保密钥长度 >= 256 位
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            keyBytes = padded;
        }
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /** 生成 JWT token（含权限列表） */
    public String generateToken(Long userId, String username, List<String> permissions) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("permissions", permissions != null ? permissions : Collections.emptyList())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + TOKEN_VALIDITY_MS))
                .signWith(secretKey)
                .compact();
    }

    /** 生成 JWT token（无权限时向后兼容的重载） */
    public String generateToken(Long userId, String username) {
        return generateToken(userId, username, Collections.emptyList());
    }

    /** 从 token 解析用户 ID */
    public Long getUserIdFromToken(String token) {
        try {
            return Long.parseLong(getClaims(token).getSubject());
        } catch (Exception e) {
            return null;
        }
    }

    /** 从 token 解析用户名 */
    public String getUsernameFromToken(String token) {
        try {
            return getClaims(token).get("username", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    /** 解析 Claims */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** 校验 token 是否有效 */
    public boolean validateToken(String token) {
        return getUserIdFromToken(token) != null;
    }

    /** 从 token 中提取权限列表 */
    @SuppressWarnings("unchecked")
    public List<String> getPermissionsFromToken(String token) {
        try {
            return getClaims(token).get("permissions", List.class);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
