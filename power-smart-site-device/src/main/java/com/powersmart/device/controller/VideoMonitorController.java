package com.powersmart.device.controller;

import com.powersmart.common.entity.Result;
import com.powersmart.device.entity.VideoMonitor;
import com.powersmart.device.service.VideoMonitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 视频监控管理 — 对接同业电力（tongye）前端 build/videoMonitor/* + build/ysy/*
 */
@RestController
@RequestMapping("/build")
@RequiredArgsConstructor
public class VideoMonitorController {

    private final VideoMonitorService videoMonitorService;

    /**
     * 萤石云获取 AccessToken（模拟）
     * 实际部署时替换为萤石云开放平台 API 调用
     */
    @PostMapping("/ysy/getAccessToken")
    public Result<Map<String, String>> getAccessToken() {
        return Result.ok(videoMonitorService.getAccessToken());
    }

    /**
     * 按设备位置查询摄像头列表
     */
    @GetMapping("/videoMonitor/queryByParentId/{parentId}")
    public Result<List<VideoMonitor>> queryByParentId(@PathVariable Long parentId) {
        return Result.ok(videoMonitorService.queryByParentId(parentId));
    }

    @PostMapping("/videoMonitor/add")
    public Result<Void> add(@RequestBody VideoMonitor entity) {
        videoMonitorService.add(entity);
        return Result.ok();
    }

    @PostMapping("/videoMonitor/edit")
    public Result<Void> edit(@RequestBody VideoMonitor entity) {
        videoMonitorService.edit(entity);
        return Result.ok();
    }

    @PostMapping("/videoMonitor/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        videoMonitorService.delete(id);
        return Result.ok();
    }

    @PostMapping("/videoMonitor/removeById/{id}")
    public Result<Void> removeById(@PathVariable Long id) {
        videoMonitorService.delete(id);
        return Result.ok();
    }
}
