package com.powersmart.system.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.entity.PageResult;
import com.powersmart.system.entity.SysConfig;
import com.powersmart.system.mapper.SysConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 系统配置服务 — 封装 AdminConfigController 的业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminConfigService {

    private final SysConfigMapper mapper;

    /**
     * 根据 configName 查询单条配置
     */
    public SysConfig queryConfigByName(Map<String, Object> params) {
        String configName = params != null && params.get("configName") != null
                ? params.get("configName").toString() : null;
        if (StrUtil.isBlank(configName)) {
            return null;
        }
        return mapper.selectOne(
                new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigName, configName));
    }

    /**
     * 分页查询系统配置
     */
    public PageResult<SysConfig> queryAdminConfig(Map<String, Object> params) {
        int p = 1, s = 20;
        if (params != null) {
            if (params.get("page") != null) try { p = Integer.parseInt(params.get("page").toString()); } catch (NumberFormatException ignored) {}
            if (params.get("limit") != null) try { s = Integer.parseInt(params.get("limit").toString()); } catch (NumberFormatException ignored) {}
        }
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<SysConfig>()
                .orderByDesc(SysConfig::getCreateTime);
        return PageResult.from(mapper.selectPage(new Page<>(p, s), wrapper));
    }

    /**
     * 新增或更新配置
     */
    public void setAdminConfig(SysConfig entity) {
        if (entity.getId() != null) {
            mapper.updateById(entity);
        } else {
            mapper.insert(entity);
        }
    }

    /**
     * 获取欢迎词配置列表
     */
    public List<SysConfig> getLogWelcomeSpeechList() {
        return mapper.selectList(
                new LambdaQueryWrapper<SysConfig>()
                        .eq(SysConfig::getConfigName, "logWelcomeSpeech"));
    }

    /**
     * 新增或更新欢迎词配置
     */
    public void setLogWelcomeSpeech(SysConfig entity) {
        if (entity.getId() != null) {
            mapper.updateById(entity);
        } else {
            if (StrUtil.isBlank(entity.getConfigName())) {
                entity.setConfigName("logWelcomeSpeech");
            }
            mapper.insert(entity);
        }
    }
}
