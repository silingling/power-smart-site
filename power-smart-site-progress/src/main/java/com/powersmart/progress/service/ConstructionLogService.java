package com.powersmart.progress.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powersmart.common.annotation.OperateLog;
import com.powersmart.common.auth.SecurityContext;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.util.PageHelper;
import com.powersmart.progress.entity.ConstructionLog;
import com.powersmart.progress.mapper.ConstructionLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
@OperateLog(module = "施工日志")
public class ConstructionLogService {

    private final ConstructionLogMapper constructionLogMapper;

    /**
     * 分页查询施工日志
     */
    public PageResult<ConstructionLog> list(Map<String, Object> params) {
        IPage<ConstructionLog> pageParam = PageHelper.of(params);
        LambdaQueryWrapper<ConstructionLog> wrapper = new LambdaQueryWrapper<>();

        if (params != null) {
            if (params.get("projectId") != null) {
                wrapper.eq(ConstructionLog::getProjectId, params.get("projectId"));
            }
            if (params.get("logDate") != null) {
                wrapper.eq(ConstructionLog::getLogDate, params.get("logDate"));
            }
            if (StrUtil.isNotBlank((String) params.get("status"))) {
                wrapper.eq(ConstructionLog::getStatus, params.get("status"));
            }
            if (StrUtil.isNotBlank((String) params.get("recorder"))) {
                wrapper.eq(ConstructionLog::getRecorder, params.get("recorder"));
            }
        }
        wrapper.orderByDesc(ConstructionLog::getLogDate, ConstructionLog::getId);
        return PageResult.from(constructionLogMapper.selectPage(pageParam, wrapper));
    }

    /**
     * 根据 ID 查询施工日志
     */
    public ConstructionLog getById(Long id) {
        return constructionLogMapper.selectById(id);
    }

    /**
     * 新增施工日志
     */
    @OperateLog(action = "add", description = "新增施工日志")
    public ConstructionLog add(ConstructionLog entity) {
        if (entity.getLogDate() == null) {
            throw new IllegalArgumentException("日志日期不能为空");
        }
        entity.setRecorder(String.valueOf(SecurityContext.getCurrentUserId()));
        entity.setRecorderName(SecurityContext.getCurrentUsername());
        entity.setStatus("draft");
        constructionLogMapper.insert(entity);
        return entity;
    }

    /**
     * 更新施工日志（仅更新非空字段）
     */
    @OperateLog(action = "update", description = "更新施工日志")
    public ConstructionLog update(ConstructionLog entity) {
        if (entity.getId() == null) {
            throw new IllegalArgumentException("日志ID不能为空");
        }
        constructionLogMapper.updateById(entity);
        return constructionLogMapper.selectById(entity.getId());
    }

    /**
     * 删除施工日志（逻辑删除）
     */
    @OperateLog(action = "delete", description = "删除施工日志")
    public void delete(Long id) {
        constructionLogMapper.deleteById(id);
    }

    /**
     * 提交施工日志
     */
    @OperateLog(action = "submit", description = "提交施工日志")
    public ConstructionLog submit(Long id) {
        ConstructionLog entity = constructionLogMapper.selectById(id);
        if (entity == null) {
            throw new IllegalArgumentException("日志不存在");
        }
        if (!"draft".equals(entity.getStatus())) {
            throw new IllegalArgumentException("仅草稿状态的日志可以提交");
        }
        // 校验必填字段
        if (entity.getLogDate() == null) {
            throw new IllegalArgumentException("日志日期不能为空");
        }
        if (StrUtil.isBlank(entity.getWeather())) {
            throw new IllegalArgumentException("天气不能为空");
        }
        if (StrUtil.isBlank(entity.getConstructionContent())) {
            throw new IllegalArgumentException("施工内容不能为空");
        }
        entity.setStatus("submitted");
        constructionLogMapper.updateById(entity);
        return entity;
    }

    /**
     * 审批施工日志
     */
    @OperateLog(action = "approve", description = "审批施工日志")
    public ConstructionLog approve(Long id, Long signatoryId, String signatoryName) {
        ConstructionLog entity = constructionLogMapper.selectById(id);
        if (entity == null) {
            throw new IllegalArgumentException("日志不存在");
        }
        if (!"submitted".equals(entity.getStatus())) {
            throw new IllegalArgumentException("仅待审批状态的日志可以审批");
        }
        entity.setStatus("approved");
        entity.setSignatoryId(signatoryId);
        entity.setSignatoryName(signatoryName);
        entity.setSignedAt(LocalDateTime.now());
        constructionLogMapper.updateById(entity);
        return entity;
    }

    /**
     * 按日期获取施工日志
     */
    public ConstructionLog getByDate(Long projectId, LocalDate date) {
        return constructionLogMapper.selectOne(new LambdaQueryWrapper<ConstructionLog>()
                .eq(ConstructionLog::getProjectId, projectId)
                .eq(ConstructionLog::getLogDate, date));
    }

    /**
     * 施工日志统计
     */
    public Map<String, Object> getLogStats(Long projectId) {
        Map<String, Object> stats = new HashMap<>();
        long total = constructionLogMapper.selectCount(new LambdaQueryWrapper<ConstructionLog>()
                .eq(ConstructionLog::getProjectId, projectId));
        long draftCount = constructionLogMapper.selectCount(new LambdaQueryWrapper<ConstructionLog>()
                .eq(ConstructionLog::getProjectId, projectId)
                .eq(ConstructionLog::getStatus, "draft"));
        long submittedCount = constructionLogMapper.selectCount(new LambdaQueryWrapper<ConstructionLog>()
                .eq(ConstructionLog::getProjectId, projectId)
                .eq(ConstructionLog::getStatus, "submitted"));
        long approvedCount = constructionLogMapper.selectCount(new LambdaQueryWrapper<ConstructionLog>()
                .eq(ConstructionLog::getProjectId, projectId)
                .eq(ConstructionLog::getStatus, "approved"));
        stats.put("total", total);
        stats.put("draftCount", draftCount);
        stats.put("submittedCount", submittedCount);
        stats.put("approvedCount", approvedCount);
        return stats;
    }
}
