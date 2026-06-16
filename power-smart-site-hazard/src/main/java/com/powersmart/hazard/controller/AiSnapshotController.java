package com.powersmart.hazard.controller;

import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.hazard.entity.AiSnapshot;
import com.powersmart.hazard.service.AiSnapshotService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AI 抓拍快照 — 同业电力前端 build/aiSnapshot/*
 */
@RestController
@RequestMapping("/build/aiSnapshot")
@RequiredArgsConstructor
public class AiSnapshotController {

    private final AiSnapshotService aiSnapshotService;

    /** 分页查询抓拍列表 */
    @PostMapping("/list")
    public Result<PageResult<AiSnapshot>> list(@RequestBody(required = false) Map<String, Object> params) {
        return Result.ok(aiSnapshotService.list(params));
    }

    /** 获取抓拍详情 */
    @PostMapping("/get/{id}")
    public Result<AiSnapshot> get(@PathVariable Long id) {
        return Result.ok(aiSnapshotService.getById(id));
    }
}
