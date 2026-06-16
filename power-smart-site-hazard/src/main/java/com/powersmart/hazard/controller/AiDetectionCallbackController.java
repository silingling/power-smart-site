package com.powersmart.hazard.controller;

import com.powersmart.common.annotation.OperateLog;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.hazard.entity.AiDetectionCallback;
import com.powersmart.hazard.service.AiDetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AI 检测回调 — 同业电力前端 build/aiDetectionCallback/*
 */
@Slf4j
@RestController
@RequestMapping("/build/aiDetectionCallback")
@RequiredArgsConstructor
public class AiDetectionCallbackController {

    private final AiDetectionService aiDetectionService;

    /** 外部 AI 推送检测结果 */
    @PostMapping("/receive")
    @OperateLog(module = "AI检测", action = "receive", description = "接收AI检测回调")
    public Result<Map<String, Object>> receive(@RequestBody Map<String, Object> callbackData) {
        AiDetectionCallback callback = aiDetectionService.processCallback(callbackData);
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("id", callback.getId());
        result.put("callbackId", callback.getCallbackId());
        result.put("processed", false);
        return Result.ok(result);
    }

    /** 分页查询回调列表 */
    @PostMapping("/list")
    public Result<PageResult<AiDetectionCallback>> list(@RequestBody(required = false) Map<String, Object> params) {
        return Result.ok(aiDetectionService.getCallbackList(params));
    }

    /** 确认检测 → 自动创建隐患 */
    @PostMapping("/confirm/{id}")
    @OperateLog(module = "AI检测", action = "confirm", description = "确认AI检测结果 #{{id}}")
    public Result<Void> confirm(@PathVariable Long id) {
        aiDetectionService.confirmDetection(id);
        return Result.ok();
    }

    /** 标记为误报 */
    @PostMapping("/dismiss/{id}")
    @OperateLog(module = "AI检测", action = "dismiss", description = "标记AI检测为误报 #{{id}}")
    public Result<Void> dismiss(@PathVariable Long id) {
        aiDetectionService.dismissDetection(id);
        return Result.ok();
    }
}
