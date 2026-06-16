package com.powersmart.progress.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powersmart.common.annotation.OperateLog;
import com.powersmart.common.auth.SecurityContext;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.util.PageHelper;
import com.powersmart.progress.entity.SafetyLog;
import com.powersmart.progress.mapper.SafetyLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
@OperateLog(module = "安全日志")
public class SafetyLogService {

    private final SafetyLogMapper safetyLogMapper;

    /**
     * 分页查询安全日志
     */
    public PageResult<SafetyLog> list(Map<String, Object> params) {
        IPage<SafetyLog> pageParam = PageHelper.of(params);
        LambdaQueryWrapper<SafetyLog> wrapper = new LambdaQueryWrapper<>();

        if (params != null) {
            if (params.get("projectId") != null) {
                wrapper.eq(SafetyLog::getProjectId, params.get("projectId"));
            }
            if (params.get("logDate") != null) {
                wrapper.eq(SafetyLog::getLogDate, params.get("logDate"));
            }
            if (StrUtil.isNotBlank((String) params.get("status"))) {
                wrapper.eq(SafetyLog::getStatus, params.get("status"));
            }
            if (StrUtil.isNotBlank((String) params.get("recorder"))) {
                wrapper.eq(SafetyLog::getRecorder, params.get("recorder"));
            }
        }
        wrapper.orderByDesc(SafetyLog::getLogDate, SafetyLog::getId);
        return PageResult.from(safetyLogMapper.selectPage(pageParam, wrapper));
    }

    /**
     * 根据 ID 查询安全日志
     */
    public SafetyLog getById(Long id) {
        return safetyLogMapper.selectById(id);
    }

    /**
     * 新增安全日志
     */
    @OperateLog(action = "add", description = "新增安全日志")
    public SafetyLog add(SafetyLog entity) {
        if (entity.getLogDate() == null) {
            throw new IllegalArgumentException("日志日期不能为空");
        }
        entity.setRecorder(String.valueOf(SecurityContext.getCurrentUserId()));
        entity.setRecorderName(SecurityContext.getCurrentUsername());
        entity.setStatus("draft");
        safetyLogMapper.insert(entity);
        return entity;
    }

    /**
     * 更新安全日志（仅更新非空字段）
     */
    @OperateLog(action = "update", description = "更新安全日志")
    public SafetyLog update(SafetyLog entity) {
        if (entity.getId() == null) {
            throw new IllegalArgumentException("日志ID不能为空");
        }
        safetyLogMapper.updateById(entity);
        return safetyLogMapper.selectById(entity.getId());
    }

    /**
     * 删除安全日志（逻辑删除）
     */
    @OperateLog(action = "delete", description = "删除安全日志")
    public void delete(Long id) {
        safetyLogMapper.deleteById(id);
    }

    /**
     * 提交安全日志
     */
    @OperateLog(action = "submit", description = "提交安全日志")
    public SafetyLog submit(Long id) {
        SafetyLog entity = safetyLogMapper.selectById(id);
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
        if (StrUtil.isBlank(entity.getSafetyEducation()) && StrUtil.isBlank(entity.getHazardCheck())) {
            throw new IllegalArgumentException("安全教育或隐患排查内容不能同时为空");
        }
        entity.setStatus("submitted");
        safetyLogMapper.updateById(entity);
        return entity;
    }

    /**
     * 审批安全日志
     */
    @OperateLog(action = "approve", description = "审批安全日志")
    public SafetyLog approve(Long id, Long signatoryId, String signatoryName) {
        SafetyLog entity = safetyLogMapper.selectById(id);
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
        safetyLogMapper.updateById(entity);
        return entity;
    }

    /**
     * 按日期获取安全日志
     */
    public SafetyLog getByDate(Long projectId, LocalDate date) {
        return safetyLogMapper.selectOne(new LambdaQueryWrapper<SafetyLog>()
                .eq(SafetyLog::getProjectId, projectId)
                .eq(SafetyLog::getLogDate, date));
    }

    /**
     * 安全日志统计
     */
    public Map<String, Object> getLogStats(Long projectId) {
        Map<String, Object> stats = new HashMap<>();
        long total = safetyLogMapper.selectCount(new LambdaQueryWrapper<SafetyLog>()
                .eq(SafetyLog::getProjectId, projectId));
        long draftCount = safetyLogMapper.selectCount(new LambdaQueryWrapper<SafetyLog>()
                .eq(SafetyLog::getProjectId, projectId)
                .eq(SafetyLog::getStatus, "draft"));
        long submittedCount = safetyLogMapper.selectCount(new LambdaQueryWrapper<SafetyLog>()
                .eq(SafetyLog::getProjectId, projectId)
                .eq(SafetyLog::getStatus, "submitted"));
        long approvedCount = safetyLogMapper.selectCount(new LambdaQueryWrapper<SafetyLog>()
                .eq(SafetyLog::getProjectId, projectId)
                .eq(SafetyLog::getStatus, "approved"));
        stats.put("total", total);
        stats.put("draftCount", draftCount);
        stats.put("submittedCount", submittedCount);
        stats.put("approvedCount", approvedCount);
        return stats;
    }
}
