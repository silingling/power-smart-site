package com.powersmart.hazard.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.common.util.PageHelper;
import com.powersmart.hazard.entity.AiViolation;
import com.powersmart.hazard.mapper.AiViolationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * AI 违规识别记录 服务层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiViolationService {

    private final AiViolationMapper aiViolationMapper;

    public PageResult<AiViolation> list(Map<String, Object> params) {
        Page<AiViolation> page = PageHelper.of(params);
        LambdaQueryWrapper<AiViolation> wrapper = new LambdaQueryWrapper<>();
        if (params != null) {
            if (params.containsKey("projectId") && params.get("projectId") != null)
                wrapper.eq(AiViolation::getProjectId, Long.valueOf(params.get("projectId").toString()));
            if (params.containsKey("status") && params.get("status") != null)
                wrapper.eq(AiViolation::getStatus, Integer.valueOf(params.get("status").toString()));
            if (params.containsKey("violationType") && params.get("violationType") != null && StrUtil.isNotBlank(params.get("violationType").toString()))
                wrapper.eq(AiViolation::getViolationType, params.get("violationType").toString());
        }
        wrapper.orderByDesc(AiViolation::getCreatedAt);
        return PageResult.from(aiViolationMapper.selectPage(page, wrapper));
    }

    public AiViolation getById(Long id) {
        return aiViolationMapper.selectById(id);
    }

    public void handle(Long id) {
        AiViolation violation = aiViolationMapper.selectById(id);
        if (violation != null) {
            violation.setStatus(1);
            violation.setHandledTime(LocalDateTime.now());
            aiViolationMapper.updateById(violation);
        }
    }
}
