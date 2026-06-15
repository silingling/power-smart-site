package com.powersmart.common.util;

import cn.hutool.core.util.StrUtil;
import com.powersmart.common.entity.PageResult;

/**
 * 分页工具类
 */
public class PageUtil {

    /**
     * 从请求参数中提取分页信息，构建 MyBatis-Plus Page 对象
     */
    public static <T> com.baomidou.mybatisplus.extension.plugins.pagination.Page<T>
    buildPage(Integer page, Integer pageSize) {
        if (page == null || page < 1) page = 1;
        if (pageSize == null || pageSize < 1 || pageSize > 100) pageSize = 20;
        return new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, pageSize);
    }

    /**
     * 将 MyBatis-Plus Page 转换为统一分页响应
     */
    public static <T> PageResult<T> toPageResult(
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<T> mpPage) {
        return PageResult.of(mpPage.getRecords(), mpPage.getTotal(),
                (int) mpPage.getCurrent(), (int) mpPage.getSize());
    }
}
