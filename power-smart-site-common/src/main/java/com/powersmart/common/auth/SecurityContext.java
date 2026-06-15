package com.powersmart.common.auth;

import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * 安全上下文 — 存储当前登录用户信息
 *
 * 在 AuthFilter 中解析 JWT 后填充 ThreadLocal，
 * 注解处理器和 Controller 层通过静态方法获取。
 */
public class SecurityContext {

    private static final ThreadLocal<Context> CONTEXT = ThreadLocal.withInitial(Context::new);

    public static void setCurrentUser(Long userId, String username, List<String> permissions) {
        Context ctx = CONTEXT.get();
        ctx.setUserId(userId);
        ctx.setUsername(username);
        ctx.setPermissions(permissions != null ? permissions : Collections.emptyList());
    }

    public static Long getCurrentUserId() {
        return CONTEXT.get().getUserId();
    }

    public static String getCurrentUsername() {
        return CONTEXT.get().getUsername();
    }

    public static List<String> getCurrentPermissions() {
        return CONTEXT.get().getPermissions();
    }

    public static boolean hasPermission(String permissionKey) {
        return CONTEXT.get().getPermissions().contains(permissionKey);
    }

    public static void clear() {
        CONTEXT.remove();
    }

    @Data
    private static class Context {
        private Long userId;
        private String username;
        private List<String> permissions = Collections.emptyList();
    }
}
