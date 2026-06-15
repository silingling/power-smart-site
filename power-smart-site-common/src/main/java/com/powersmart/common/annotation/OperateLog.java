package com.powersmart.common.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 *
 * <p>标注在 Controller 方法上，AOP 自动记录操作日志到 operate_log 表。
 * 支持 SpEL 表达式提取参数值。</p>
 *
 * <pre>{@code
 *   @OperateLog(module = "隐患管理", action = "update", targetType = "HazardReport",
 *               description = "修改隐患 #{{#id}}", targetId = "{{#id}}")
 *   public Result<Void> update(@PathVariable Long id, @RequestBody HazardReport report) { ... }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperateLog {

    /** 功能模块名称（如：隐患管理、人员管理、设备管理） */
    String module() default "";

    /** 操作类型（insert / update / delete / export / login / audit / other） */
    String action() default "";

    /** 操作描述，支持 {{#paramName}} SpEL 占位 */
    String description() default "";

    /** 操作对象类型（表名或业务类型） */
    String targetType() default "";

    /** 操作对象 ID，支持 {{#paramName}} SpEL 占位 */
    String targetId() default "";

    /** 是否记录请求参数（默认 true） */
    boolean recordParams() default true;

    /** 是否记录返回结果（默认 false，避免大数据量） */
    boolean recordResult() default false;
}
