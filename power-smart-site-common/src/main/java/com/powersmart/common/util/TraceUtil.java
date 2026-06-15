package com.powersmart.common.util;

import cn.hutool.core.lang.UUID;

/**
 * 链路追踪工具类
 */
public class TraceUtil {

    private static final ThreadLocal<String> TRACE_ID_HOLDER = new ThreadLocal<>();

    public static String generateTraceId() {
        return UUID.fastUUID().toString(true);
    }

    public static String getTraceId() {
        String traceId = TRACE_ID_HOLDER.get();
        if (traceId == null) {
            traceId = generateTraceId();
            setTraceId(traceId);
        }
        return traceId;
    }

    public static void setTraceId(String traceId) {
        TRACE_ID_HOLDER.set(traceId);
    }

    public static void clear() {
        TRACE_ID_HOLDER.remove();
    }
}
