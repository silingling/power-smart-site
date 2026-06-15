package com.powersmart.system.controller;

import com.powersmart.common.entity.Result;
import com.powersmart.system.entity.EvalLevel;
import com.powersmart.system.service.EvalLevelService;
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

    private final EvalLevelService evalLevelService;

    @PostMapping("/selectList")
    public Result<List<EvalLevel>> selectList(@RequestBody(required = false) Map<String, Object> params) {
        return Result.ok(evalLevelService.selectList(params));
    }
}
