package com.powersmart.system.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.system.entity.SysConfig;
import com.powersmart.system.mapper.SysConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 系统配置管理 — 同业电力前端 adminConfig/*
 */
@RestController
@RequiredArgsConstructor
public class AdminConfigController {

    private final SysConfigMapper mapper;

    @PostMapping("/adminConfig/queryFirstConfigByName")
    public Result<SysConfig> queryFirstConfigByName(@RequestBody Map<String, Object> params) {
        return queryConfigByName(params);
    }

    @PostMapping("/adminConfig/queryConfigByName")
    public Result<SysConfig> queryConfigByName(@RequestBody Map<String, Object> params) {
        String configName = params != null && params.get("configName") != null
                ? params.get("configName").toString() : null;
        if (StrUtil.isBlank(configName)) {
            return Result.fail("configName 不能为空");
        }
        SysConfig config = mapper.selectOne(
                new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigName, configName));
        return Result.ok(config);
    }

    @PostMapping("/adminConfig/queryAdminConfig")
    public Result<PageResult<SysConfig>> queryAdminConfig(@RequestBody Map<String, Object> params) {
        int p = 1, s = 20;
        if (params != null) {
            if (params.get("page") != null) try { p = Integer.parseInt(params.get("page").toString()); } catch (NumberFormatException ignored) {}
            if (params.get("limit") != null) try { s = Integer.parseInt(params.get("limit").toString()); } catch (NumberFormatException ignored) {}
        }
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<SysConfig>()
                .orderByDesc(SysConfig::getCreateTime);
        return Result.ok(PageResult.from(mapper.selectPage(new Page<>(p, s), wrapper)));
    }

    @PostMapping("/adminConfig/setAdminConfig")
    public Result<Void> setAdminConfig(@RequestBody SysConfig entity) {
        if (entity.getId() != null) {
            mapper.updateById(entity);
        } else {
            mapper.insert(entity);
        }
        return Result.ok();
    }

    @PostMapping("/adminConfig/getLogWelcomeSpeechList")
    public Result<List<SysConfig>> getLogWelcomeSpeechList() {
        List<SysConfig> list = mapper.selectList(
                new LambdaQueryWrapper<SysConfig>()
                        .eq(SysConfig::getConfigName, "logWelcomeSpeech"));
        return Result.ok(list);
    }

    @PostMapping("/adminConfig/setLogWelcomeSpeech")
    public Result<Void> setLogWelcomeSpeech(@RequestBody SysConfig entity) {
        if (entity.getId() != null) {
            mapper.updateById(entity);
        } else {
            if (StrUtil.isBlank(entity.getConfigName())) {
                entity.setConfigName("logWelcomeSpeech");
            }
            mapper.insert(entity);
        }
        return Result.ok();
    }
}
