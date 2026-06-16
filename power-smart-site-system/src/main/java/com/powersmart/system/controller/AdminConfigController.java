package com.powersmart.system.controller;

import cn.hutool.core.util.StrUtil;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.system.entity.SysConfig;
import com.powersmart.system.service.AdminConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统配置管理 — 同业电力前端 adminConfig/*
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/adminConfig")
public class AdminConfigController {

    private final AdminConfigService adminConfigService;

    @PostMapping("/queryFirstConfigByName")
    public Result<SysConfig> queryFirstConfigByName(@RequestBody Map<String, Object> params) {
        return queryConfigByName(params);
    }

    @PostMapping("/queryConfigByName")
    public Result<SysConfig> queryConfigByName(@RequestBody Map<String, Object> params) {
        String configName = params != null && params.get("configName") != null
                ? params.get("configName").toString() : null;
        if (StrUtil.isBlank(configName)) {
            return Result.fail("configName 不能为空");
        }
        return Result.ok(adminConfigService.queryConfigByName(params));
    }

    @PostMapping("/queryAdminConfig")
    public Result<PageResult<SysConfig>> queryAdminConfig(@RequestBody Map<String, Object> params) {
        return Result.ok(adminConfigService.queryAdminConfig(params));
    }

    @PostMapping("/setAdminConfig")
    public Result<Void> setAdminConfig(@RequestBody SysConfig entity) {
        adminConfigService.setAdminConfig(entity);
        return Result.ok();
    }

    @PostMapping("/getLogWelcomeSpeechList")
    public Result<List<SysConfig>> getLogWelcomeSpeechList() {
        return Result.ok(adminConfigService.getLogWelcomeSpeechList());
    }

    @PostMapping("/setLogWelcomeSpeech")
    public Result<Void> setLogWelcomeSpeech(@RequestBody SysConfig entity) {
        adminConfigService.setLogWelcomeSpeech(entity);
        return Result.ok();
    }
}
