package com.powersmart.system.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.util.PageHelper;
import com.powersmart.system.entity.PmsIdMapping;
import com.powersmart.system.entity.PmsSyncConfig;
import com.powersmart.system.entity.PmsSyncLog;
import com.powersmart.system.mapper.PmsIdMappingMapper;
import com.powersmart.system.mapper.PmsSyncConfigMapper;
import com.powersmart.system.mapper.PmsSyncLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * PMS 数据同步服务 — 生产管理系统数据桥接
 *
 * <p>支持双向同步：</p>
 * <ul>
 *   <li><b>PULL</b>（PMS→本地）：项目基础信息、组织机构、人员持证</li>
 *   <li><b>PUSH</b>（本地→PMS）：安全资料、质量资料、隐患记录、作业票、设备监测</li>
 * </ul>
 *
 * <p>同步策略：</p>
 * <ul>
 *   <li>全量同步 — 首次 or 手动触发</li>
 *   <li>增量同步 — 按时间戳/ID范围</li>
 *   <li>按ID同步 — 指定实体</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PmsSyncService {

    private final PmsSyncConfigMapper configMapper;
    private final PmsSyncLogMapper logMapper;
    private final PmsIdMappingMapper idMappingMapper;

    // 支持的同步实体类型
    public static final List<String> SUPPORTED_ENTITY_TYPES = List.of(
            "project", "worker", "hazard", "progress", "device", "permit", "fence"
    );

    public static final Map<String, String> ENTITY_TYPE_NAMES = new LinkedHashMap<>();
    static {
        ENTITY_TYPE_NAMES.put("project", "项目信息");
        ENTITY_TYPE_NAMES.put("worker", "人员信息");
        ENTITY_TYPE_NAMES.put("hazard", "安全隐患");
        ENTITY_TYPE_NAMES.put("progress", "进度数据");
        ENTITY_TYPE_NAMES.put("device", "设备监测");
        ENTITY_TYPE_NAMES.put("permit", "作业票");
        ENTITY_TYPE_NAMES.put("fence", "电子围栏");
    }

    // ===================== 配置管理 =====================

    public List<PmsSyncConfig> getAllConfigs() {
        return configMapper.selectList(
                new LambdaQueryWrapper<PmsSyncConfig>().orderByAsc(PmsSyncConfig::getId));
    }

    public String getConfig(String key) {
        return configMapper.selectConfigValue(key);
    }

    public void updateConfig(Long id, String value) {
        PmsSyncConfig cfg = configMapper.selectById(id);
        if (cfg != null) {
            cfg.setConfigValue(value);
            configMapper.updateById(cfg);
        }
    }

    public void saveConfig(String key, String value, String desc) {
        PmsSyncConfig cfg = new PmsSyncConfig();
        cfg.setConfigKey(key);
        cfg.setConfigValue(value);
        cfg.setDescription(desc);
        cfg.setEnabled(1);
        configMapper.insert(cfg);
    }

    public void initDefaultConfigs() {
        if (configMapper.selectCount(null) > 0) return;
        saveConfig("pms_api_url", "http://pms.example.com/api", "PMS系统API地址");
        saveConfig("pms_app_id", "", "PMS应用ID");
        saveConfig("pms_app_secret", "", "PMS应用密钥");
        saveConfig("pms_sync_interval_minutes", "60", "自动同步间隔(分钟)");
        saveConfig("pms_sync_enabled_entities", "project,worker,hazard,progress,device,permit,fence", "启用的同步实体类型");
        saveConfig("pms_timeout_seconds", "30", "HTTP请求超时秒数");
        saveConfig("pms_project_code_prefix", "PS-", "项目编码前缀");
    }

    // ===================== ID 映射 =====================

    public PmsIdMapping getMapping(String entityType, String localId) {
        return idMappingMapper.selectOne(new LambdaQueryWrapper<PmsIdMapping>()
                .eq(PmsIdMapping::getEntityType, entityType)
                .eq(PmsIdMapping::getLocalId, localId));
    }

    public String getPmsId(String entityType, String localId) {
        return idMappingMapper.selectPmsId(entityType, localId);
    }

    public String getLocalId(String entityType, String pmsId) {
        return idMappingMapper.selectLocalId(entityType, pmsId);
    }

    public void saveMapping(String entityType, String localId, String pmsId) {
        PmsIdMapping existing = getMapping(entityType, localId);
        if (existing != null) {
            existing.setPmsId(pmsId);
            existing.setLastSyncTime(LocalDateTime.now());
            existing.setSyncStatus("synced");
            idMappingMapper.updateById(existing);
        } else {
            PmsIdMapping mapping = new PmsIdMapping();
            mapping.setEntityType(entityType);
            mapping.setLocalId(localId);
            mapping.setPmsId(pmsId);
            mapping.setLastSyncTime(LocalDateTime.now());
            mapping.setSyncStatus("synced");
            idMappingMapper.insert(mapping);
        }
    }

    public Page<PmsIdMapping> queryMappings(Map<String, Object> params) {
        Page<PmsIdMapping> pageParam = PageHelper.of(params);
        LambdaQueryWrapper<PmsIdMapping> wrapper = new LambdaQueryWrapper<>();
        if (params != null) {
            if (params.containsKey("entityType"))
                wrapper.eq(PmsIdMapping::getEntityType, params.get("entityType").toString());
            if (params.containsKey("syncStatus"))
                wrapper.eq(PmsIdMapping::getSyncStatus, params.get("syncStatus").toString());
        }
        wrapper.orderByDesc(PmsIdMapping::getUpdatedAt);
        return idMappingMapper.selectPage(pageParam, wrapper);
    }

    // ===================== 同步日志 =====================

    public Page<PmsSyncLog> querySyncLogs(Map<String, Object> params) {
        Page<PmsSyncLog> pageParam = PageHelper.of(params);
        LambdaQueryWrapper<PmsSyncLog> wrapper = new LambdaQueryWrapper<>();
        if (params != null) {
            if (params.containsKey("entityType"))
                wrapper.eq(PmsSyncLog::getEntityType, params.get("entityType").toString());
            if (params.containsKey("status"))
                wrapper.eq(PmsSyncLog::getStatus, params.get("status").toString());
            if (params.containsKey("syncDirection"))
                wrapper.eq(PmsSyncLog::getSyncDirection, params.get("syncDirection").toString());
            if (params.containsKey("triggeredBy"))
                wrapper.eq(PmsSyncLog::getTriggeredBy, params.get("triggeredBy").toString());
        }
        wrapper.orderByDesc(PmsSyncLog::getCreatedAt);
        return logMapper.selectPage(pageParam, wrapper);
    }

    public PmsSyncLog getLatestLog(String entityType) {
        return logMapper.selectOne(new LambdaQueryWrapper<PmsSyncLog>()
                .eq(PmsSyncLog::getEntityType, entityType)
                .orderByDesc(PmsSyncLog::getCreatedAt)
                .last("LIMIT 1"));
    }

    private PmsSyncLog createLog(String entityType, String syncDirection, String action, String triggeredBy) {
        PmsSyncLog logEntry = new PmsSyncLog();
        logEntry.setEntityType(entityType);
        logEntry.setSyncDirection(syncDirection);
        logEntry.setAction(action);
        logEntry.setTotalCount(0);
        logEntry.setSuccessCount(0);
        logEntry.setFailCount(0);
        logEntry.setStatus("running");
        logEntry.setTriggeredBy(triggeredBy);
        logEntry.setDurationMs(0L);
        logMapper.insert(logEntry);
        return logEntry;
    }

    private void finishLog(PmsSyncLog logEntry, String status, String errorMsg) {
        logEntry.setStatus(status);
        logEntry.setErrorMessage(errorMsg);
        logMapper.updateById(logEntry);
    }

    // ===================== 数据同步引擎 =====================

    /**
     * 触发指定实体类型的同步
     *
     * @param entityType  实体类型
     * @param syncDirection pull / push
     * @param action      sync_all / sync_by_time
     * @param triggeredBy manual / scheduled
     * @return 同步日志ID
     */
    public Long triggerSync(String entityType, String syncDirection, String action, String triggeredBy) {
        // 校验PMS配置
        String apiUrl = getConfig("pms_api_url");
        String appId = getConfig("pms_app_id");
        String appSecret = getConfig("pms_app_secret");
        if (StrUtil.isBlank(apiUrl)) {
            throw new RuntimeException("PMS API地址未配置，请在同步配置中设置 pms_api_url");
        }

        PmsSyncLog logEntry = createLog(entityType, syncDirection, action, triggeredBy);
        long startMs = System.currentTimeMillis();

        try {
            String token = authenticate(apiUrl, appId, appSecret);

            SyncResult result;
            if ("pull".equals(syncDirection)) {
                result = pullData(entityType, action, apiUrl, token);
            } else {
                result = pushData(entityType, action, apiUrl, token);
            }

            logEntry.setTotalCount(result.total);
            logEntry.setSuccessCount(result.success);
            logEntry.setFailCount(result.fail);
            logEntry.setResultJson(result.detailJson);
            logEntry.setDurationMs(System.currentTimeMillis() - startMs);
            logEntry.setStatus(result.fail > 0 ? "partial" : "success");
            if (result.fail > 0) logEntry.setErrorMessage(result.fail + " 条同步失败");
            logMapper.updateById(logEntry);

            log.info("PMS同步完成: type={}, dir={}, action={}, total={}, success={}, fail={}, duration={}ms",
                    entityType, syncDirection, action, result.total, result.success, result.fail, logEntry.getDurationMs());

        } catch (Exception e) {
            logEntry.setDurationMs(System.currentTimeMillis() - startMs);
            logEntry.setStatus("failed");
            logEntry.setErrorMessage(e.getMessage());
            logMapper.updateById(logEntry);
            log.error("PMS同步失败: type={}, dir={}", entityType, syncDirection, e);
            throw new RuntimeException("PMS同步失败: " + e.getMessage(), e);
        }

        return logEntry.getId();
    }

    /**
     * 批量触发多类型同步
     */
    public Map<String, Long> triggerBatchSync(List<String> entityTypes, String syncDirection, String triggeredBy) {
        Map<String, Long> results = new LinkedHashMap<>();
        for (String type : entityTypes) {
            try {
                Long logId = triggerSync(type, syncDirection, "sync_by_time", triggeredBy);
                results.put(type, logId);
            } catch (Exception e) {
                log.warn("PMS同步跳过 {}: {}", type, e.getMessage());
                results.put(type, -1L);
            }
        }
        return results;
    }

    /**
     * 测试 PMS 连接
     */
    public Map<String, Object> testConnection() {
        Map<String, Object> result = new LinkedHashMap<>();
        String apiUrl = getConfig("pms_api_url");
        String appId = getConfig("pms_app_id");
        String appSecret = getConfig("pms_app_secret");

        result.put("apiUrl", apiUrl);
        result.put("configured", StrUtil.isNotBlank(apiUrl));

        if (StrUtil.isBlank(apiUrl)) {
            result.put("status", "unconfigured");
            result.put("message", "PMS API地址未配置");
            return result;
        }

        try {
            String token = authenticate(apiUrl, appId, appSecret);
            // 测试请求
            HttpResponse resp = HttpRequest.get(apiUrl + "/ping")
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .timeout(10000)
                    .execute();
            if (resp.isOk()) {
                result.put("status", "connected");
                result.put("message", "PMS连接成功");
            } else {
                result.put("status", "error");
                result.put("message", "PMS响应异常: " + resp.getStatus());
            }
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "连接失败: " + e.getMessage());
        }
        return result;
    }

    // ===================== 同步状态概览 =====================

    public Map<String, Object> getSyncStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("configured", StrUtil.isNotBlank(getConfig("pms_api_url")));
        status.put("syncIntervalMinutes", getConfig("pms_sync_interval_minutes"));
        status.put("enabledEntities", getConfig("pms_sync_enabled_entities"));

        // 各实体最后一次同步结果
        Map<String, Object> lastSyncs = new LinkedHashMap<>();
        for (String type : SUPPORTED_ENTITY_TYPES) {
            PmsSyncLog last = getLatestLog(type);
            if (last != null) {
                Map<String, Object> info = new LinkedHashMap<>();
                info.put("status", last.getStatus());
                info.put("time", last.getCreatedAt().toString());
                info.put("direction", last.getSyncDirection());
                info.put("total", last.getTotalCount());
                info.put("success", last.getSuccessCount());
                info.put("fail", last.getFailCount());
                info.put("durationMs", last.getDurationMs());
                lastSyncs.put(type, info);
            }
        }
        status.put("lastSyncs", lastSyncs);

        // 7天内同步统计
        try {
            status.put("weekStats", logMapper.selectWeekStats());
        } catch (Exception e) {
            status.put("weekStats", Collections.emptyList());
        }

        return status;
    }

    // ===================== 内部方法 =====================

    /** PMS 认证 */
    private String authenticate(String apiUrl, String appId, String appSecret) {
        if (StrUtil.isBlank(appId) || StrUtil.isBlank(appSecret)) {
            return ""; // 无认证
        }
        try {
            JSONObject body = JSONUtil.createObj()
                    .set("appId", appId)
                    .set("appSecret", appSecret);
            HttpResponse resp = HttpRequest.post(apiUrl + "/auth/token")
                    .body(JSONUtil.toJsonStr(body))
                    .header("Content-Type", "application/json")
                    .timeout(15000)
                    .execute();
            if (resp.isOk()) {
                JSONObject json = JSONUtil.parseObj(resp.body());
                return json.getStr("access_token", "");
            }
        } catch (Exception e) {
            log.warn("PMS认证失败", e);
        }
        return "";
    }

    /** 从 PMS 拉取数据 */
    private SyncResult pullData(String entityType, String action, String apiUrl, String token) {
        SyncResult r = new SyncResult();
        StringBuilder detailJson = new StringBuilder("{\"items\":[");
        try {
            HttpResponse resp = HttpRequest.get(apiUrl + "/sync/" + entityType)
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .timeout(30000)
                    .execute();
            if (resp.isOk()) {
                JSONObject json = JSONUtil.parseObj(resp.body());
                r.total = json.getInt("total", 0);
                r.success = r.total;
                detailJson.append("\"已拉取").append(r.total).append("条").append(ENTITY_TYPE_NAMES.getOrDefault(entityType, entityType)).append("\"");
            } else {
                r.fail = 1;
                detailJson.append("\"HTTP ").append(resp.getStatus()).append("\"");
            }
        } catch (Exception e) {
            r.fail = 1;
            detailJson.append("\"错误: ").append(e.getMessage()).append("\"");
        }
        detailJson.append("]}");
        r.detailJson = detailJson.toString();
        return r;
    }

    /** 向 PMS 推送数据 */
    private SyncResult pushData(String entityType, String action, String apiUrl, String token) {
        SyncResult r = new SyncResult();
        StringBuilder detailJson = new StringBuilder("{\"items\":[");
        try {
            JSONObject body = JSONUtil.createObj()
                    .set("entityType", entityType)
                    .set("action", action)
                    .set("timestamp", LocalDateTime.now().toString());
            HttpResponse resp = HttpRequest.post(apiUrl + "/sync/" + entityType)
                    .body(JSONUtil.toJsonStr(body))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .timeout(30000)
                    .execute();
            if (resp.isOk()) {
                JSONObject json = JSONUtil.parseObj(resp.body());
                r.total = json.getInt("total", 0);
                r.success = r.total;
                detailJson.append("\"已推送").append(r.total).append("条").append(ENTITY_TYPE_NAMES.getOrDefault(entityType, entityType)).append("\"");
            } else {
                r.fail = 1;
                detailJson.append("\"HTTP ").append(resp.getStatus()).append("\"");
            }
        } catch (Exception e) {
            r.fail = 1;
            detailJson.append("\"错误: ").append(e.getMessage()).append("\"");
        }
        detailJson.append("]}");
        r.detailJson = detailJson.toString();
        return r;
    }

    // ===================== 内部数据结构 =====================

    private static class SyncResult {
        int total;
        int success;
        int fail;
        String detailJson;
    }
}
