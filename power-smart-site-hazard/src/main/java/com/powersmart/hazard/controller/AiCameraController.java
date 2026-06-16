package com.powersmart.hazard.controller;

import com.powersmart.common.annotation.OperateLog;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.hazard.entity.AiCamera;
import com.powersmart.hazard.service.AiCameraService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI 摄像头 — 同业电力前端 build/aiCamera/*
 */
@RestController
@RequestMapping("/build/aiCamera")
@RequiredArgsConstructor
public class AiCameraController {

    private final AiCameraService aiCameraService;

    /** 分页查询摄像头列表 */
    @PostMapping("/list")
    public Result<PageResult<AiCamera>> list(@RequestBody(required = false) Map<String, Object> params) {
        return Result.ok(aiCameraService.queryPage(params));
    }

    /** 获取摄像头详情 */
    @PostMapping("/get/{id}")
    public Result<AiCamera> get(@PathVariable Long id) {
        return Result.ok(aiCameraService.getById(id));
    }

    /** 新增摄像头 */
    @PostMapping("/add")
    @OperateLog(module = "AI摄像头", action = "insert", description = "新增摄像头")
    public Result<Void> add(@RequestBody AiCamera camera) {
        aiCameraService.add(camera);
        return Result.ok();
    }

    /** 修改摄像头 */
    @PostMapping("/set")
    @OperateLog(module = "AI摄像头", action = "update", description = "修改摄像头")
    public Result<Void> set(@RequestBody AiCamera camera) {
        aiCameraService.edit(camera);
        return Result.ok();
    }

    /** 删除摄像头 */
    @PostMapping("/del/{id}")
    @OperateLog(module = "AI摄像头", action = "delete", description = "删除摄像头 #{{id}}", targetId = "{{id}}")
    public Result<Void> del(@PathVariable Long id) {
        aiCameraService.delete(id);
        return Result.ok();
    }

    /** 更新摄像头在线状态 */
    @PostMapping("/updateStatus")
    @OperateLog(module = "AI摄像头", action = "update", description = "更新摄像头状态")
    public Result<Void> updateStatus(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Integer status = Integer.valueOf(params.get("status").toString());
        aiCameraService.updateStatus(id, status);
        return Result.ok();
    }

    /** 按项目查询摄像头列表 */
    @PostMapping("/getByProject")
    public Result<List<AiCamera>> getByProject(@RequestBody Map<String, Object> params) {
        Long projectId = Long.valueOf(params.get("projectId").toString());
        return Result.ok(aiCameraService.getByProject(projectId));
    }
}
