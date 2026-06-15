package com.powersmart.system.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.annotation.OperateLog;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.system.entity.PmsIdMapping;
import com.powersmart.system.entity.PmsSyncConfig;
import com.powersmart.system.entity.PmsSyncLog;
import com.powersmart.system.service.PmsSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * PMS 数据同步接口 — /build/pmsSync/*
 *
 * <p>与生产管理系统(PMS)的双向数据同步桥接</p>
 */
@RestController
@RequiredArgsConstructor
public class PmsSyncController {

    private final PmsSyncService syncService;

    // ==================== 同步触发 ====================

    /** 触发指定实体类型的同步 */
    @PostMapping("/build/pmsSync/triggerSync")
    @OperateLog(module = "PMS数据同步", action = "sync", description = "手动触发单实体同步", recordResult = false)
    public Result<Map<String, Object>> triggerSync(@RequestBody Map<String, Object> params) {
        String entityType = params.containsKey("entityType") ? params.get("entityType").toString() : null;
        String syncDirection = params.containsKey("syncDirection") ? params.get("syncDirection").toString() : "pull";
        String action = params.containsKey("action") ? params.get("action").toString() : "sync_by_time";
        String triggeredBy = params.containsKey("triggeredBy") ? params.get("triggeredBy").toString() : "manual";

        if (StrUtil.isBlank(entityType)) {
            return Result.fail("entityType 不能为空，可选: " + String.join(",", PmsSyncService.SUPPORTED_ENTITY_TYPES));
        }
        if (!PmsSyncService.SUPPORTED_ENTITY_TYPES.contains(entityType)) {
            return Result.fail("不支持的实体类型: " + entityType);
        }

        try {
            Long logId = syncService.triggerSync(entityType, syncDirection, action, triggeredBy);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("logId", logId);
            result.put("entityType", entityType);
            result.put("syncDirection", syncDirection);
            result.put("status", "triggered");
            return Result.ok(result);
        } catch (Exception e) {
            return Result.fail("同步触发失败: " + e.getMessage());
        }
    }

    /** 批量触发同步 */
    @PostMapping("/build/pmsSync/triggerBatchSync")
    @OperateLog(module = "PMS数据同步", action = "sync", description = "手动触发全量同步", recordResult = false)
    public Result<Map<String, Object>> triggerBatchSync(@RequestBody Map<String, Object> params) {
        String entityTypesStr = params.containsKey("entityTypes") ? params.get("entityTypes").toString() : "";
        String syncDirection = params.containsKey("syncDirection") ? params.get("syncDirection").toString() : "pull";
        String triggeredBy = params.containsKey("triggeredBy") ? params.get("triggeredBy").toString() : "manual";

        List<String> entityTypes;
        if (StrUtil.isNotBlank(entityTypesStr)) {
            entityTypes = Arrays.stream(entityTypesStr.split(","))
                    .map(String::trim)
                    .filter(PmsSyncService.SUPPORTED_ENTITY_TYPES::contains)
                    .collect(Collectors.toList());
        } else {
            // 默认启用的实体类型
            String enabled = syncService.getConfig("pms_sync_enabled_entities");
            if (StrUtil.isNotBlank(enabled)) {
                entityTypes = Arrays.asList(enabled.split(","));
            } else {
                entityTypes = PmsSyncService.SUPPORTED_ENTITY_TYPES;
            }
        }

        Map<String, Long> results = syncService.triggerBatchSync(entityTypes, syncDirection, triggeredBy);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("results", results);
        result.put("total", results.size());
        result.put("successCount", results.values().stream().filter(v -> v > 0).count());
        result.put("syncDirection", syncDirection);
        return Result.ok(result);
    }

    // ==================== 同步日志 ====================

    /** 分页查询同步日志 */
    @PostMapping("/build/pmsSync/querySyncLogList")
    public Result<PageResult<Map<String, Object>>> querySyncLogs(@RequestBody(required = false) Map<String, Object> params) {
        Page<PmsSyncLog> page = syncService.querySyncLogs(params);
        List<Map<String, Object>> list = page.getRecords().stream().map(log -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", log.getId());
            m.put("entityType", log.getEntityType());
            m.put("entityTypeName", PmsSyncService.ENTITY_TYPE_NAMES.getOrDefault(log.getEntityType(), log.getEntityType()));
            m.put("syncDirection", log.getSyncDirection());
            m.put("action", log.getAction());
            m.put("totalCount", log.getTotalCount());
            m.put("successCount", log.getSuccessCount());
            m.put("failCount", log.getFailCount());
            m.put("status", log.getStatus());
            m.put("errorMessage", log.getErrorMessage());
            m.put("durationMs", log.getDurationMs());
            m.put("triggeredBy", log.getTriggeredBy());
            m.put("createdAt", log.getCreatedAt() != null ? log.getCreatedAt().toString() : "");
            return m;
        }).collect(Collectors.toList());
        return Result.ok(PageResult.of(list, page.getTotal(), (int) page.getCurrent(), (int) page.getSize()));
    }

    /** 获取同步日志详情 */
    @PostMapping("/build/pmsSync/getSyncLog/{id}")
    public Result<Map<String, Object>> getSyncLog(@PathVariable Long id) {
        PmsSyncLog log = syncService.querySyncLogs(Map.of("page", "1", "pageSize", "1")).getRecords().stream()
                .filter(l -> l.getId().equals(id)).findFirst().orElse(null);
        if (log == null) return Result.fail("同步日志不存在");
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", log.getId());
        m.put("entityType", log.getEntityType());
        m.put("syncDirection", log.getSyncDirection());
        m.put("action", log.getAction());
        m.put("entityIds", log.getEntityIds());
        m.put("pmsIds", log.getPmsIds());
        m.put("totalCount", log.getTotalCount());
        m.put("successCount", log.getSuccessCount());
        m.put("failCount", log.getFailCount());
        m.put("status", log.getStatus());
        m.put("errorMessage", log.getErrorMessage());
        m.put("resultJson", log.getResultJson());
        m.put("durationMs", log.getDurationMs());
        m.put("triggeredBy", log.getTriggeredBy());
        m.put("createdAt", log.getCreatedAt() != null ? log.getCreatedAt().toString() : "");
        return Result.ok(m);
    }

    // ==================== 同步配置 ====================

    /** 获取所有同步配置 */
    @PostMapping("/build/pmsSync/getSyncConfigList")
    public Result<List<Map<String, Object>>> getConfigList() {
        List<PmsSyncConfig> configs = syncService.getAllConfigs();
        List<Map<String, Object>> list = configs.stream().map(cfg -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", cfg.getId());
            m.put("configKey", cfg.getConfigKey());
            m.put("configValue", cfg.getConfigValue());
            m.put("description", cfg.getDescription());
            m.put("enabled", cfg.getEnabled());
            m.put("createdAt", cfg.getCreatedAt() != null ? cfg.getCreatedAt().toString() : "");
            return m;
        }).collect(Collectors.toList());
        return Result.ok(list);
    }

    /** 更新同步配置 */
    @PostMapping("/build/pmsSync/setSyncConfig")
    @OperateLog(module = "PMS数据同步", action = "update", description = "更新同步配置")
    public Result<Void> setConfig(@RequestBody Map<String, Object> params) {
        Long id = params.containsKey("id") ? Long.parseLong(params.get("id").toString()) : null;
        String configValue = params.containsKey("configValue") ? params.get("configValue").toString() : null;
        if (id == null || configValue == null) return Result.fail("id 和 configValue 不能为空");
        syncService.updateConfig(id, configValue);
        return Result.ok();
    }

    // ==================== ID 映射 ====================

    /** 查询 ID 映射 */
    @PostMapping("/build/pmsSync/queryIdMappingList")
    public Result<PageResult<Map<String, Object>>> queryMappings(@RequestBody(required = false) Map<String, Object> params) {
        Page<PmsIdMapping> page = syncService.queryMappings(params);
        List<Map<String, Object>> list = page.getRecords().stream().map(m -> {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("id", m.getId());
            r.put("entityType", m.getEntityType());
            r.put("localId", m.getLocalId());
            r.put("pmsId", m.getPmsId());
            r.put("lastSyncTime", m.getLastSyncTime() != null ? m.getLastSyncTime().toString() : "");
            r.put("syncStatus", m.getSyncStatus());
            return r;
        }).collect(Collectors.toList());
        return Result.ok(PageResult.of(list, page.getTotal(), (int) page.getCurrent(), (int) page.getSize()));
    }

    // ==================== 连接测试 & 状态 ====================

    /** 测试 PMS 连接 */
    @PostMapping("/build/pmsSync/testPmsConnection")
    public Result<Map<String, Object>> testConnection() {
        return Result.ok(syncService.testConnection());
    }

    /** 同步状态概览 */
    @PostMapping("/build/pmsSync/getPmsSyncStatus")
    public Result<Map<String, Object>> getStatus() {
        return Result.ok(syncService.getSyncStatus());
    }

    /** 获取支持的实体类型列表 */
    @PostMapping("/build/pmsSync/getSyncEntityTypeList")
    public Result<List<Map<String, Object>>> getEntityTypes() {
        List<Map<String, Object>> list = PmsSyncService.SUPPORTED_ENTITY_TYPES.stream()
                .map(type -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("code", type);
                    m.put("name", PmsSyncService.ENTITY_TYPE_NAMES.getOrDefault(type, type));
                    return m;
                }).collect(Collectors.toList());
        return Result.ok(list);
    }
}
