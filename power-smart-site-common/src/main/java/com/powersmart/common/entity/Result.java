package com.powersmart.common.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一响应结果
 * <p>
 * 兼容萤丰 YFConstruction 前端格式：
 * 成功 → { code: 0, data: {...}, msg: "操作成功" }
 * 失败 → { code: 非0, msg: "错误信息" }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {

    /** 业务状态码：0=成功，非0=失败 */
    private int code;

    /** 提示消息，前端通过 msg 读取 */
    @JsonProperty("msg")
    private String message;

    /** 响应数据 */
    private T data;

    /** 链路追踪ID（仅 debug 时使用） */
    private String traceId;

    // ====== 成功 ======

    public static <T> Result<T> ok(T data) {
        return new Result<>(0, "操作成功", data, null);
    }

    public static <T> Result<T> ok() {
        return new Result<>(0, "操作成功", null, null);
    }

    public static <T> Result<T> ok(String msg, T data) {
        return new Result<>(0, msg, data, null);
    }

    // ====== 失败 ======

    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null, null);
    }

    public static <T> Result<T> fail(String message) {
        return new Result<>(500, message, null, null);
    }

    public static <T> Result<T> fail(String message, T data) {
        return new Result<>(500, message, data, null);
    }

    // ====== 链式 ======

    public Result<T> withTraceId(String traceId) {
        this.traceId = traceId;
        return this;
    }
}
