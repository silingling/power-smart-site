package com.powersmart.common.annotation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.powersmart.common.entity.OperateLog;
import com.powersmart.common.service.OperateLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 操作日志 AOP 切面
 *
 * <p>拦截所有标注 {@link OperateLog} 的 Controller 方法，自动记录：
 * <ul>
 *   <li>操作人 + IP + UA</li>
 *   <li>请求参数（脱敏）</li>
 *   <li>执行耗时 + 成功/失败</li>
 * </ul>
 * </p>
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperateLogAspect {

    private final OperateLogService operateLogService;
    private final ObjectMapper objectMapper;
    private final ExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(operateLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperateLog operateLog) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 解析注解元数据
        String module = operateLog.module();
        String action = operateLog.action();
        String description = operateLog.description();
        String targetType = operateLog.targetType();
        String targetIdExpr = operateLog.targetId();

        // 请求信息
        HttpServletRequest request = getRequest();
        String ip = request != null ? getClientIp(request) : null;
        String userAgent = request != null ? request.getHeader("User-Agent") : null;

        // 解析 SpEL
        Object[] args = joinPoint.getArgs();
        String targetId = resolveSpel(targetIdExpr, joinPoint);

        // 请求参数
        String paramsJson = null;
        if (operateLog.recordParams() && args.length > 0) {
            try {
                paramsJson = objectMapper.writeValueAsString(buildParamsMap(args));
            } catch (Exception e) {
                log.debug("序列化请求参数失败", e);
            }
        }

        // 操作人
        Long operatorId = null;
        String operatorName = null;
        try {
            operatorId = com.powersmart.common.auth.SecurityContext.getCurrentUserId();
            operatorName = com.powersmart.common.auth.SecurityContext.getCurrentUsername();
        } catch (Exception e) {
            // 无登录上下文时（如登录接口本身）跳过
        }

        String finalDescription = description;
        if (finalDescription.contains("{{")) {
            finalDescription = resolveSpel(finalDescription, joinPoint);
        }

        Object result;
        OperateLog logEntry = new OperateLog();
        try {
            result = joinPoint.proceed();
            long cost = System.currentTimeMillis() - startTime;

            logEntry.setModule(module);
            logEntry.setAction(action);
            logEntry.setDescription(finalDescription);
            logEntry.setOperatorId(operatorId);
            logEntry.setOperatorName(operatorName);
            logEntry.setTargetType(targetType);
            logEntry.setTargetId(targetId);
            logEntry.setRequestParams(paramsJson);
            logEntry.setIp(ip);
            logEntry.setUserAgent(userAgent);
            logEntry.setDurationMs((int) cost);
            logEntry.setStatus(1);
            logEntry.setCreateTime(LocalDateTime.now());

            if (operateLog.recordResult()) {
                logEntry.setResult(objectMapper.writeValueAsString(result));
            }

            operateLogService.save(logEntry);
            return result;

        } catch (Throwable e) {
            long cost = System.currentTimeMillis() - startTime;

            logEntry.setModule(module);
            logEntry.setAction(action);
            logEntry.setDescription(finalDescription);
            logEntry.setOperatorId(operatorId);
            logEntry.setOperatorName(operatorName);
            logEntry.setTargetType(targetType);
            logEntry.setTargetId(targetId);
            logEntry.setRequestParams(paramsJson);
            logEntry.setIp(ip);
            logEntry.setUserAgent(userAgent);
            logEntry.setDurationMs((int) cost);
            logEntry.setStatus(0);
            logEntry.setErrorMsg(e.getMessage() != null ? e.getMessage().substring(0, Math.min(e.getMessage().length(), 500)) : "未知异常");
            logEntry.setCreateTime(LocalDateTime.now());

            operateLogService.save(logEntry);
            throw e;
        }
    }

    private String resolveSpel(String expression, ProceedingJoinPoint joinPoint) {
        if (expression == null || expression.isEmpty()) return null;
        if (!expression.contains("{{") && !expression.contains("#")) return expression;

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        StandardEvaluationContext ctx = new StandardEvaluationContext();
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length && i < args.length; i++) {
                ctx.setVariable(paramNames[i], args[i]);
            }
        }

        // 替换 {{#name}} → #name
        String spel = expression.replace("{{", "#").replace("}}", "");
        try {
            return parser.parseExpression(spel).getValue(ctx, String.class);
        } catch (Exception e) {
            log.debug("SpEL 解析失败: expr={}", expression, e);
            return expression;
        }
    }

    private Map<String, Object> buildParamsMap(Object[] args) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg == null) continue;
            // 跳过 HttpServletRequest/Response
            if (arg instanceof HttpServletRequest || arg instanceof jakarta.servlet.http.HttpServletResponse) continue;
            map.put("arg" + i, arg);
        }
        return map;
    }

    private HttpServletRequest getRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
