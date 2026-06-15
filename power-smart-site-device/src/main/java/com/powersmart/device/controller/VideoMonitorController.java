package com.powersmart.device.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.powersmart.common.entity.Result;
import com.powersmart.device.entity.VideoMonitor;
import com.powersmart.device.mapper.VideoMonitorMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 视频监控管理 — 对接同业电力（tongye）前端 build/videoMonitor/* + build/ysy/*
 */
@RestController
@RequestMapping("/build")
@RequiredArgsConstructor
public class VideoMonitorController {

    private final VideoMonitorMapper mapper;

    /**
     * 萤石云获取 AccessToken（模拟）
     * 实际部署时替换为萤石云开放平台 API 调用
     */
    @PostMapping("/ysy/getAccessToken")
    public Result<Map<String, String>> getAccessToken() {
        String token = UUID.randomUUID().toString().replace("-", "");
        return Result.ok(Map.of(
                "accessToken", token,
                "expireTime", "7200"
        ));
    }

    /**
     * 按设备位置查询摄像头列表
     */
    @PostMapping("/videoMonitor/queryByParentId/{parentId}")
    @GetMapping("/videoMonitor/queryByParentId/{parentId}")
    public Result<List<VideoMonitor>> queryByParentId(@PathVariable Long parentId) {
        LambdaQueryWrapper<VideoMonitor> wrapper = new LambdaQueryWrapper<VideoMonitor>()
                .eq(VideoMonitor::getLocationId, parentId)
                .orderByDesc(VideoMonitor::getCreatedAt);
        return Result.ok(mapper.selectList(wrapper));
    }

    @PostMapping("/videoMonitor/add")
    public Result<Void> add(@RequestBody VideoMonitor entity) {
        mapper.insert(entity);
        return Result.ok();
    }

    @PostMapping("/videoMonitor/edit")
    public Result<Void> edit(@RequestBody VideoMonitor entity) {
        mapper.updateById(entity);
        return Result.ok();
    }

    @PostMapping("/videoMonitor/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        mapper.deleteById(id);
        return Result.ok();
    }
}
