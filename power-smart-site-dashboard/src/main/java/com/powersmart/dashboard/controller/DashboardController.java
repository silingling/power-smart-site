package com.powersmart.dashboard.controller;

import com.powersmart.common.entity.Result;
import com.powersmart.common.push.SsePushService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据看板 — /api/v1/dashboard/*
 *
 * <p>提供项目级实时统计、趋势图表、分布分析等数据聚合接口。
 * 所有 SQL 使用参数化查询，禁止字符串拼接。</p>
 *
 * <p>扩充内容（Phase 5F）：</p>
 * <ul>
 *   <li>实时数据聚合</li>
 *   <li>隐患/告警趋势（按天）</li>
 *   <li>隐患分布（按类型/等级）</li>
 *   <li>人员出勤统计</li>
 *   <li>设备在线率趋势</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final JdbcTemplate jt;
    private final SsePushService ssePushService;

    // =============================================================
    //  1. 项目总览摘要
    // =============================================================

    @GetMapping("/project/{projectId}/summary")
    public Result<Map<String, Object>> getProjectSummary(@PathVariable Long projectId) {
        Map<String, Object> s = new LinkedHashMap<>();

        // — 人员 —
        s.put("totalWorkers",      qc("SELECT COUNT(*) FROM worker WHERE project_id=?", projectId));
        s.put("activeWorkers",     qc("SELECT COUNT(*) FROM worker WHERE project_id=? AND status=1", projectId));
        s.put("todayAttendance",   qc("SELECT COUNT(DISTINCT worker_id) FROM attendance_record WHERE project_id=? AND attend_date=CURDATE()", projectId));

        // — 隐患 —
        s.put("totalHazards",      qc("SELECT COUNT(*) FROM hazard_report WHERE project_id=?", projectId));
        s.put("openHazards",       qc("SELECT COUNT(*) FROM hazard_report WHERE project_id=? AND status IN (1,2)", projectId));
        s.put("closedHazards",     qc("SELECT COUNT(*) FROM hazard_report WHERE project_id=? AND status IN (3,4)", projectId));
        s.put("todayViolations",   qc("SELECT COUNT(*) FROM hazard_report WHERE project_id=? AND report_type=1 AND DATE(created_at)=CURDATE()", projectId));
        s.put("criticalHazards",   qc("SELECT COUNT(*) FROM hazard_report WHERE project_id=? AND hazard_level>=3 AND status IN (1,2)", projectId));

        // — 设备 —
        s.put("totalDevices",      qc("SELECT COUNT(*) FROM device WHERE project_id=?", projectId));
        s.put("onlineDevices",     qc("SELECT COUNT(*) FROM device WHERE project_id=? AND status='online'", projectId));
        s.put("alarmDevices",      qc("SELECT COUNT(DISTINCT device_id) FROM device_alarm WHERE project_id=? AND status=0", projectId));
        s.put("onlineRate",        calcRate("SELECT COUNT(*) FROM device WHERE project_id=?", projectId,
                                            "SELECT COUNT(*) FROM device WHERE project_id=? AND status='online'", projectId));

        // — 进度 —
        s.put("totalTasks",        qc("SELECT COUNT(*) FROM progress_task WHERE project_id=?", projectId));
        s.put("delayTasks",        qc("SELECT COUNT(*) FROM progress_task WHERE project_id=? AND status='delayed'", projectId));
        s.put("taskProgress",      calcProgress(projectId));

        // — 安全 —
        s.put("activePermits",     qc("SELECT COUNT(*) FROM special_work_permit WHERE project_id=? AND status='active'", projectId));
        s.put("fenceAlerts",       qc("SELECT COUNT(*) FROM fence_alert_event WHERE project_id=? AND status='pending'", projectId));

        // — 实时状态 —
        s.put("sseOnline",         ssePushService.getOnlineCount());
        s.put("todayAlarms",       qc("SELECT COUNT(*) FROM device_alarm WHERE project_id=? AND DATE(create_time)=CURDATE()", projectId));

        // — 今日操作活跃 —
        s.put("todayOperations",   qc("SELECT COUNT(*) FROM operate_log WHERE DATE(create_time)=CURDATE()", projectId));

        return Result.ok(s);
    }

    // =============================================================
    //  2. 隐患趋势（最近 N 天）
    // =============================================================

    @GetMapping("/project/{projectId}/hazardTrend")
    public Result<List<Map<String, Object>>> hazardTrend(@PathVariable Long projectId,
                                                          @RequestParam(defaultValue = "7") int days) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate d = LocalDate.now().minusDays(i);
            String date = d.format(DateTimeFormatter.ISO_LOCAL_DATE);
            long total = qc("SELECT COUNT(*) FROM hazard_report WHERE project_id=? AND DATE(created_at)=?", projectId, date);
            long ai    = qc("SELECT COUNT(*) FROM hazard_report WHERE project_id=? AND report_type=1 AND DATE(created_at)=?", projectId, date);
            long manual = qc("SELECT COUNT(*) FROM hazard_report WHERE project_id=? AND report_type=2 AND DATE(created_at)=?", projectId, date);
            long closed = qc("SELECT COUNT(*) FROM hazard_report WHERE project_id=? AND status IN (3,4) AND DATE(updated_at)=?", projectId, date);
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("date", date);
            point.put("total", total);
            point.put("aiDetected", ai);
            point.put("manualReport", manual);
            point.put("closed", closed);
            list.add(point);
        }
        return Result.ok(list);
    }

    // =============================================================
    //  3. 隐患分布（按类型 Top-N）
    // =============================================================

    @GetMapping("/project/{projectId}/hazardDistribution")
    public Result<List<Map<String, Object>>> hazardDistribution(@PathVariable Long projectId,
                                                                  @RequestParam(defaultValue = "10") int topN) {
        List<Map<String, Object>> rows = jt.queryForList(
                "SELECT hazard_type AS name, COUNT(*) AS value FROM hazard_report WHERE project_id=? " +
                "GROUP BY hazard_type ORDER BY value DESC LIMIT ?", projectId, topN);
        return Result.ok(rows);
    }

    // =============================================================
    //  4. 隐患等级分布
    // =============================================================

    @GetMapping("/project/{projectId}/hazardLevelDistribution")
    public Result<List<Map<String, Object>>> hazardLevelDistribution(@PathVariable Long projectId) {
        List<Map<String, Object>> rows = jt.queryForList(
                "SELECT CASE hazard_level WHEN 1 THEN '一般' WHEN 2 THEN '较大' WHEN 3 THEN '重大' WHEN 4 THEN '特别重大' ELSE '未知' END AS name, " +
                "COUNT(*) AS value FROM hazard_report WHERE project_id=? GROUP BY hazard_level ORDER BY hazard_level",
                projectId);
        return Result.ok(rows);
    }

    // =============================================================
    //  5. 告警趋势（最近 N 天）
    // =============================================================

    @GetMapping("/project/{projectId}/alarmTrend")
    public Result<List<Map<String, Object>>> alarmTrend(@PathVariable Long projectId,
                                                         @RequestParam(defaultValue = "7") int days) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate d = LocalDate.now().minusDays(i);
            String date = d.format(DateTimeFormatter.ISO_LOCAL_DATE);
            long total    = qc("SELECT COUNT(*) FROM device_alarm WHERE project_id=? AND DATE(create_time)=?", projectId, date);
            long warning  = qc("SELECT COUNT(*) FROM device_alarm WHERE project_id=? AND alarm_level='warning' AND DATE(create_time)=?", projectId, date);
            long critical = qc("SELECT COUNT(*) FROM device_alarm WHERE project_id=? AND alarm_level='critical' AND DATE(create_time)=?", projectId, date);
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("date", date);
            point.put("total", total);
            point.put("warning", warning);
            point.put("critical", critical);
            list.add(point);
        }
        return Result.ok(list);
    }

    // =============================================================
    //  6. 人员出勤统计（最近 N 天）
    // =============================================================

    @GetMapping("/project/{projectId}/attendanceTrend")
    public Result<List<Map<String, Object>>> attendanceTrend(@PathVariable Long projectId,
                                                              @RequestParam(defaultValue = "7") int days) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate d = LocalDate.now().minusDays(i);
            String date = d.format(DateTimeFormatter.ISO_LOCAL_DATE);
            long checkIn  = qc("SELECT COUNT(*) FROM attendance_record WHERE project_id=? AND attend_date=? AND status=1", projectId, date);
            long late     = qc("SELECT COUNT(*) FROM attendance_record WHERE project_id=? AND attend_date=? AND status=2", projectId, date);
            long absent   = qc("SELECT COUNT(*) FROM attendance_record WHERE project_id=? AND attend_date=? AND status=4", projectId, date);
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("date", date);
            point.put("checkIn", checkIn);
            point.put("late", late);
            point.put("absent", absent);
            list.add(point);
        }
        return Result.ok(list);
    }

    // =============================================================
    //  7. 设备在线率趋势（最近 N 天，按日采样）
    // =============================================================

    @GetMapping("/project/{projectId}/deviceOnlineTrend")
    public Result<List<Map<String, Object>>> deviceOnlineTrend(@PathVariable Long projectId,
                                                                @RequestParam(defaultValue = "7") int days) {
        long totalDevices = qc("SELECT COUNT(*) FROM device WHERE project_id=?", projectId);
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate d = LocalDate.now().minusDays(i);
            String date = d.format(DateTimeFormatter.ISO_LOCAL_DATE);
            long online = qc("SELECT COUNT(*) FROM device WHERE project_id=? AND status='online' AND " +
                             "updated_at >= ? AND updated_at < DATE_ADD(?, INTERVAL 1 DAY)", projectId, date, date);
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("date", date);
            point.put("total", totalDevices);
            point.put("online", online);
            point.put("rate", totalDevices > 0 ? Math.round(online * 100.0 / totalDevices) : 0);
            list.add(point);
        }
        return Result.ok(list);
    }

    // =============================================================
    //  8. 操作日志统计（最近 N 天）
    // =============================================================

    @GetMapping("/project/{projectId}/operationTrend")
    public Result<List<Map<String, Object>>> operationTrend(@PathVariable Long projectId,
                                                             @RequestParam(defaultValue = "7") int days) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate d = LocalDate.now().minusDays(i);
            String date = d.format(DateTimeFormatter.ISO_LOCAL_DATE);
            long count = qc("SELECT COUNT(*) FROM operate_log WHERE DATE(create_time)=?", date);
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("date", date);
            point.put("count", count);
            list.add(point);
        }
        return Result.ok(list);
    }

    // =============================================================
    //  9. SSE 实时在线人数
    // =============================================================

    @GetMapping("/realtime/onlineCount")
    public Result<Map<String, Object>> onlineCount() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("sseOnline", ssePushService.getOnlineCount());
        return Result.ok(m);
    }

    // =============================================================
    //  10. 实时告警数据（最近 N 条未处理）
    // =============================================================

    @GetMapping("/project/{projectId}/recentAlarms")
    public Result<List<Map<String, Object>>> recentAlarms(@PathVariable Long projectId,
                                                           @RequestParam(defaultValue = "10") int limit) {
        List<Map<String, Object>> rows = jt.queryForList(
                "SELECT da.id, da.alarm_type, da.alarm_level, da.alarm_value, da.description, da.status, da.create_time, " +
                "d.device_name, d.device_code " +
                "FROM device_alarm da LEFT JOIN device d ON da.device_id=d.id " +
                "WHERE da.project_id=? ORDER BY da.create_time DESC LIMIT ?",
                projectId, limit);
        return Result.ok(rows);
    }

    // =============================================================
    //  帮助方法
    // =============================================================

    /** 单参数 COUNT 查询 */
    private long qc(String sql, Long projectId) {
        Long c = jt.queryForObject(sql, Long.class, projectId);
        return c != null ? c : 0L;
    }

    /** 双参数 COUNT 查询 */
    private long qc(String sql, Long projectId, String arg2) {
        Long c = jt.queryForObject(sql, Long.class, projectId, arg2);
        return c != null ? c : 0L;
    }

    /** 三参数 COUNT 查询 */
    private long qc(String sql, Long projectId, String arg2, String arg3) {
        Long c = jt.queryForObject(sql, Long.class, projectId, arg2, arg3);
        return c != null ? c : 0L;
    }

    /** 计算比率 */
    private double calcRate(String totalSql, Long totalPid, String partSql, Long partPid) {
        long t = qc(totalSql, totalPid);
        if (t == 0) return 0.0;
        long p = qc(partSql, partPid);
        return Math.round(p * 1000.0 / t) / 10.0;
    }

    /** 进度完成比率（已完成任务数 / 总任务数） */
    private double calcProgress(Long projectId) {
        long total = qc("SELECT COUNT(*) FROM progress_task WHERE project_id=?", projectId);
        if (total == 0) return 0.0;
        long done = qc("SELECT COUNT(*) FROM progress_task WHERE project_id=? AND status='completed'", projectId);
        return Math.round(done * 1000.0 / total) / 10.0;
    }
}
