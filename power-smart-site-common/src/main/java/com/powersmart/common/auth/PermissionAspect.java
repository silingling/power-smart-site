package com.powersmart.common.auth;

import com.powersmart.common.entity.Result;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.List;

/**
 * 权限注解 AOP 切面
 *
 * <p>拦截所有标注 {@link RequirePermission} 的方法，
 * 检查当前用户的权限列表中是否包含指定的 permission_key。</p>
 */
@Slf4j
@Aspect
@Component
public class PermissionAspect {

    @Around("@annotation(requirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, RequirePermission requirePermission) throws Throwable {
        String[] requiredKeys = requirePermission.value();
        Logical logical = requirePermission.logical();
        List<String> userPermissions = SecurityContext.getCurrentPermissions();

        boolean hasPermission;
        if (logical == Logical.AND) {
            // 必须同时拥有所有权限
            hasPermission = Arrays.stream(requiredKeys).allMatch(userPermissions::contains);
        } else {
            // 拥有任意一个即可
            hasPermission = Arrays.stream(requiredKeys).anyMatch(userPermissions::contains);
        }

        if (!hasPermission) {
            log.warn("权限不足: userId={}, required={}, actual={}, method={}",
                    SecurityContext.getCurrentUserId(),
                    Arrays.toString(requiredKeys),
                    userPermissions,
                    joinPoint.getSignature().toShortString());

            // 返回 403 响应
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletResponse response = attrs.getResponse();
                if (response != null && !response.isCommitted()) {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write(
                            "{\"code\":403,\"msg\":\"权限不足，请联系管理员\",\"data\":null}");
                    return null;
                }
            }
            return Result.fail(403, "权限不足，请联系管理员");
        }

        return joinPoint.proceed();
    }
}
