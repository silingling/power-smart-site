package com.powersmart.hazard.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.hazard.entity.AiViolation;
import com.powersmart.hazard.mapper.AiViolationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * AI 违规识别记录 — 同业电力前端 build/aiViolation/*
 */
@RestController
@RequestMapping("/build/aiViolation")
@RequiredArgsConstructor
public class AiViolationController {

    private final AiViolationMapper mapper;

    @PostMapping("/list")
    public Result<PageResult<AiViolation>> list(@RequestBody(required = false) Map<String, Object> params) {
        Page<AiViolation> page = extractPage(params);
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
        return Result.ok(PageResult.from(mapper.selectPage(page, wrapper)));
    }

    @PostMapping("/getById/{id}")
    public Result<AiViolation> getById(@PathVariable Long id) {
        return Result.ok(mapper.selectById(id));
    }

    @PostMapping("/handle/{id}")
    public Result<Void> handle(@PathVariable Long id) {
        AiViolation violation = mapper.selectById(id);
        if (violation != null) {
            violation.setStatus(1);
            violation.setHandledTime(LocalDateTime.now());
            mapper.updateById(violation);
        }
        return Result.ok();
    }

    @SuppressWarnings("unchecked")
    private Page<AiViolation> extractPage(Map<String, Object> params) {
        int p = 1, s = 20;
        if (params != null) {
            if (params.get("page") != null) try { p = Integer.parseInt(params.get("page").toString()); } catch (NumberFormatException ignored) {}
            if (params.get("limit") != null) try { s = Integer.parseInt(params.get("limit").toString()); } catch (NumberFormatException ignored) {}
            if (params.get("pageSize") != null) try { s = Integer.parseInt(params.get("pageSize").toString()); } catch (NumberFormatException ignored) {}
        }
        return new Page<>(p, s);
    }
}
