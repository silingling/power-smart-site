package com.powersmart.hazard.controller;

import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.hazard.entity.AiViolation;
import com.powersmart.hazard.service.AiViolationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AI 违规识别记录 — 同业电力前端 build/aiViolation/*
 */
@RestController
@RequestMapping("/build/aiViolation")
@RequiredArgsConstructor
public class AiViolationController {

    private final AiViolationService aiViolationService;

    @PostMapping("/list")
    public Result<PageResult<AiViolation>> list(@RequestBody(required = false) Map<String, Object> params) {
        return Result.ok(aiViolationService.list(params));
    }

    @PostMapping("/getById/{id}")
    public Result<AiViolation> getById(@PathVariable Long id) {
        return Result.ok(aiViolationService.getById(id));
    }

    @PostMapping("/handle/{id}")
    public Result<Void> handle(@PathVariable Long id) {
        aiViolationService.handle(id);
        return Result.ok();
    }
}
