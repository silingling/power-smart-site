package com.powersmart.system.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.powersmart.common.entity.Result;
import com.powersmart.system.entity.EvalLevel;
import com.powersmart.system.mapper.EvalLevelMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 评价等级管理 — 同业电力前端 build/evalLevel/*
 */
@RestController
@RequestMapping("/build/evalLevel")
@RequiredArgsConstructor
public class EvalLevelController {

    private final EvalLevelMapper mapper;

    @PostMapping("/selectList")
    public Result<List<EvalLevel>> selectList(@RequestBody(required = false) Map<String, Object> params) {
        LambdaQueryWrapper<EvalLevel> wrapper = new LambdaQueryWrapper<>();
        if (params != null && params.containsKey("projectId") && params.get("projectId") != null)
            wrapper.eq(EvalLevel::getProjectId, Long.valueOf(params.get("projectId").toString()));
        wrapper.orderByAsc(EvalLevel::getScoreMin);
        return Result.ok(mapper.selectList(wrapper));
    }
}
