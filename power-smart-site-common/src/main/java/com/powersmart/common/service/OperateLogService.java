package com.powersmart.common.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powersmart.common.entity.OperateLog;
import com.powersmart.common.mapper.OperateLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 操作审计日志服务
 *
 * <p>提供日志异步保存与多维度查询能力。</p>
 */
@Service
@RequiredArgsConstructor
public class OperateLogService extends ServiceImpl<OperateLogMapper, OperateLog> {

    /**
     * 多条件分页查询
     */
    public Page<OperateLog> queryPage(int page, int size, String module, String action,
                                       Long operatorId, LocalDateTime startTime, LocalDateTime endTime,
                                       String keyword) {
        Page<OperateLog> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<OperateLog> wrapper = new LambdaQueryWrapper<OperateLog>()
                .eq(module != null && !module.isEmpty(), OperateLog::getModule, module)
                .eq(action != null && !action.isEmpty(), OperateLog::getAction, action)
                .eq(operatorId != null, OperateLog::getOperatorId, operatorId)
                .ge(startTime != null, OperateLog::getCreateTime, startTime)
                .le(endTime != null, OperateLog::getCreateTime, endTime)
                .like(keyword != null && !keyword.isEmpty(), OperateLog::getDescription, keyword)
                .orderByDesc(OperateLog::getCreateTime);
        return baseMapper.selectPage(pageParam, wrapper);
    }

    /**
     * 查询最近 N 条日志
     */
    public List<OperateLog> getRecent(int limit) {
        return baseMapper.selectList(new LambdaQueryWrapper<OperateLog>()
                .orderByDesc(OperateLog::getCreateTime)
                .last("LIMIT " + limit));
    }

    /**
     * 按模块统计操作次数
     */
    public List<java.util.Map<String, Object>> countByModule(LocalDateTime start, LocalDateTime end) {
        return baseMapper.selectMaps(new LambdaQueryWrapper<OperateLog>()
                .ge(start != null, OperateLog::getCreateTime, start)
                .le(end != null, OperateLog::getCreateTime, end)
                .select("module, COUNT(*) as cnt")
                .groupBy("module")
                .orderByDesc("cnt"));
    }
}
