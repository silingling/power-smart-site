package com.powersmart.system.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.powersmart.common.entity.Result;
import com.powersmart.system.entity.SysConfig;
import com.powersmart.system.mapper.SysConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 系统配置管理 — 同业电力前端 adminConfig/*
 */
@RestController
@RequiredArgsConstructor
public class AdminConfigController {

    private final SysConfigMapper mapper;

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
}
