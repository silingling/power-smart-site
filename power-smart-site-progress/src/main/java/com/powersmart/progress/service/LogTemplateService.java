package com.powersmart.progress.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powersmart.common.annotation.OperateLog;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.util.PageHelper;
import com.powersmart.progress.entity.LogTemplate;
import com.powersmart.progress.mapper.LogTemplateMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@OperateLog(module = "日志模板")
public class LogTemplateService {

    private final LogTemplateMapper logTemplateMapper;

    /**
     * 分页查询日志模板
     */
    public PageResult<LogTemplate> list(Map<String, Object> params) {
        IPage<LogTemplate> pageParam = PageHelper.of(params);
        LambdaQueryWrapper<LogTemplate> wrapper = new LambdaQueryWrapper<>();

        if (params != null) {
            if (params.get("projectId") != null) {
                wrapper.eq(LogTemplate::getProjectId, params.get("projectId"));
            }
            if (StrUtil.isNotBlank((String) params.get("logType"))) {
                wrapper.eq(LogTemplate::getLogType, params.get("logType"));
            }
        }
        wrapper.orderByDesc(LogTemplate::getId);
        return PageResult.from(logTemplateMapper.selectPage(pageParam, wrapper));
    }

    /**
     * 根据 ID 查询日志模板
     */
    public LogTemplate getById(Long id) {
        return logTemplateMapper.selectById(id);
    }

    /**
     * 新增日志模板
     */
    @OperateLog(action = "add", description = "新增日志模板")
    public LogTemplate add(LogTemplate entity) {
        if (StrUtil.isBlank(entity.getTemplateName())) {
            throw new IllegalArgumentException("模板名称不能为空");
        }
        if (StrUtil.isBlank(entity.getLogType())) {
            throw new IllegalArgumentException("日志类型不能为空");
        }
        logTemplateMapper.insert(entity);
        return entity;
    }

    /**
     * 更新日志模板
     */
    @OperateLog(action = "update", description = "更新日志模板")
    public LogTemplate update(LogTemplate entity) {
        if (entity.getId() == null) {
            throw new IllegalArgumentException("模板ID不能为空");
        }
        logTemplateMapper.updateById(entity);
        return logTemplateMapper.selectById(entity.getId());
    }

    /**
     * 删除日志模板（逻辑删除）
     */
    @OperateLog(action = "delete", description = "删除日志模板")
    public void delete(Long id) {
        logTemplateMapper.deleteById(id);
    }

    /**
     * 按类型获取已启用的日志模板列表
     */
    public List<LogTemplate> getByType(Long projectId, String logType) {
        return logTemplateMapper.selectList(new LambdaQueryWrapper<LogTemplate>()
                .eq(LogTemplate::getProjectId, projectId)
                .eq(LogTemplate::getLogType, logType)
                .eq(LogTemplate::getEnabled, 1));
    }
}
