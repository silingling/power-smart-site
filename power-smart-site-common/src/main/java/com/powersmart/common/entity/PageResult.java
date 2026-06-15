package com.powersmart.common.entity;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 统一分页响应
 * <p>
 * 序列化后嵌套在 Result.data 中返回：
 * { code: 0, data: { list: [...], total: 100, page: 1, pageSize: 20 }, msg: "操作成功" }
 */
@Data
public class PageResult<T> implements Serializable {
    private List<T> list;
    private long total;
    private int page;
    private int pageSize;

    public static <T> PageResult<T> of(List<T> list, long total, int page, int pageSize) {
        PageResult<T> r = new PageResult<>();
        r.setList(list);
        r.setTotal(total);
        r.setPage(page);
        r.setPageSize(pageSize);
        return r;
    }

    /** 从 MyBatis-Plus IPage 构建 */
    public static <T> PageResult<T> from(IPage<T> mpPage) {
        return of(mpPage.getRecords(), mpPage.getTotal(),
                (int) mpPage.getCurrent(), (int) mpPage.getSize());
    }

    /** 从普通 List 构建全量数据（无分页） */
    public static <T> PageResult<T> all(List<T> list) {
        return of(list, list.size(), 1, list.size());
    }
}
