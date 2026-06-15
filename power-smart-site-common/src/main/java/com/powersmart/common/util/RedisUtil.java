package com.powersmart.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * Redis 工具类
 *
 * 职责：
 * - Token 黑名单管理（登录态失效、强制下线）
 * - 分布式锁 / 缓存（等后续扩展）
 *
 * 使用方式：
 *   RedisUtil.set("key", "value", 3600);
 *   RedisUtil.get("key");
 *   RedisUtil.isBlacklisted("token");
 *   RedisUtil.addToBlacklist("token");
 */
@Slf4j
@Component
@ConditionalOnClass(StringRedisTemplate.class)
public class RedisUtil {

    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";
    private static final long TOKEN_BLACKLIST_TTL_SEC = 86400; // 24h（与 JWT 有效期一致）

    private static RedisUtil instance;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @PostConstruct
    public void init() {
        instance = this;
        log.info("RedisUtil 初始化完成，Token 黑名单已就绪");
    }

    // ==================== 基本操作 ====================

    public static void set(String key, String value, long timeoutSeconds) {
        if (instance == null || instance.stringRedisTemplate == null) {
            log.warn("Redis 未配置，set 操作被跳过: key={}", key);
            return;
        }
        instance.stringRedisTemplate.opsForValue().set(key, value, timeoutSeconds, TimeUnit.SECONDS);
    }

    public static String get(String key) {
        if (instance == null || instance.stringRedisTemplate == null) return null;
        return instance.stringRedisTemplate.opsForValue().get(key);
    }

    public static void delete(String key) {
        if (instance == null || instance.stringRedisTemplate == null) return;
        instance.stringRedisTemplate.delete(key);
    }

    public static boolean hasKey(String key) {
        if (instance == null || instance.stringRedisTemplate == null) return false;
        return Boolean.TRUE.equals(instance.stringRedisTemplate.hasKey(key));
    }

    // ==================== Token 黑名单 ====================

    /**
     * 将 token 加入黑名单（登出时调用）
     */
    public static void addToBlacklist(String token) {
        if (token == null || token.isEmpty()) return;
        String key = TOKEN_BLACKLIST_PREFIX + token;
        set(key, "1", TOKEN_BLACKLIST_TTL_SEC);
        log.debug("Token 已加入黑名单: {}...", token.substring(0, Math.min(20, token.length())));
    }

    /**
     * 判断 token 是否在黑名单中（已被登出）
     */
    public static boolean isBlacklisted(String token) {
        if (token == null || token.isEmpty()) return true;
        return hasKey(TOKEN_BLACKLIST_PREFIX + token);
    }

    /**
     * 从黑名单中移除 token（允许重新激活）
     */
    public static void removeFromBlacklist(String token) {
        if (token == null || token.isEmpty()) return;
        delete(TOKEN_BLACKLIST_PREFIX + token);
    }
}
