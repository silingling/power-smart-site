package com.powersmart.common.auth;

import java.lang.annotation.*;

/**
 * 权限校验注解 — 标注在 Controller 方法上
 *
 * <p>在方法执行前检查当前用户是否拥有指定的 permission_key。
 * 通过 PermissionAspect 实现 AOP 拦截。</p>
 *
 * <pre>
 * 使用示例：
 *   @RequirePermission("system:user:list")
 *   @PostMapping("/adminUser/queryUserList")
 *   public Result&lt;?&gt; queryUserList(...)
 * </pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {

    /**
     * 需要的权限标识符（permission_key）
     * 多个值之间为"且"关系（AND），必须同时拥有
     */
    String[] value();

    /**
     * 校验逻辑：AND（默认，需同时拥有全部权限）
     */
    Logical logical() default Logical.AND;

    enum Logical {
        AND, OR
    }
}
