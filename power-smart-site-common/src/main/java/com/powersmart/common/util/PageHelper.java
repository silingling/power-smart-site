package com.powersmart.common.util;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 通用分页参数解析器
 *
 * <p>统一处理同业电力（tongye）前端的分页参数格式。
 * 前端参数名支持三种风格：page / pageSize / limit。
 * 所有 Controller 优先使用此类代替手写 extractPage。</p>
 *
 * <pre>
 * 使用示例：
 *   Page<Xxx> page = PageHelper.of(params);
 *   PageResult<Xxx> result = PageResult.from(mapper.selectPage(page, wrapper));
 * </pre>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PageHelper {

    public static final int DEFAULT_PAGE = 1;
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 200;

    /**
     * 从请求参数中解析分页信息，返回 MyBatis-Plus Page 对象
     *
     * @param params 前端传来的参数 Map（可以为 null）
     * @param <T>    实体类型
     * @return MyBatis-Plus Page 对象
     */
    public static <T> Page<T> of(Map<String, Object> params) {
        int page = DEFAULT_PAGE;
        int size = DEFAULT_SIZE;

        if (params != null) {
            page = parseInt(params, "page", DEFAULT_PAGE);
            size = parseInt(params, "pageSize",
                    parseInt(params, "limit", DEFAULT_SIZE));
        }

        // 安全限幅
        page = Math.max(page, 1);
        size = Math.max(1, Math.min(size, MAX_SIZE));

        return new Page<>(page, size);
    }

    /**
     * 从请求参数中安全提取整数
     */
    private static int parseInt(Map<String, Object> params, String key, int defaultValue) {
        if (params == null || !params.containsKey(key)) return defaultValue;
        Object value = params.get(key);
        if (value == null) return defaultValue;
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    // ==================== 便捷重载（适配 lambda 风格） ====================

    /**
     * 直接传入页码和每页条数（已由调用方手动解析时使用）
     */
    public static <T> Page<T> of(int page, int size) {
        return new Page<>(Math.max(page, 1),
                Math.max(1, Math.min(size, MAX_SIZE)));
    }
}
