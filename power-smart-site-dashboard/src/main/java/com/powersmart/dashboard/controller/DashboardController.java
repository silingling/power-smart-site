package com.powersmart.dashboard.controller;

import com.powersmart.common.entity.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 项目看板 — /api/v1/dashboard/project/{projectId}/summary
 *
 * <p>安全说明：所有 SQL 使用参数化查询，禁止字符串拼接。</p>
 */
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/project/{projectId}/summary")
    public Result<Map<String, Object>> getProjectSummary(@PathVariable Long projectId) {
        Map<String, Object> summary = new HashMap<>();

        // 人员统计
        summary.put("totalWorkers", queryCount("SELECT COUNT(*) FROM worker WHERE project_id=?", projectId));
        summary.put("activeWorkers", queryCount("SELECT COUNT(*) FROM worker WHERE project_id=? AND status=1", projectId));

        // 隐患统计
        summary.put("totalHazards", queryCount("SELECT COUNT(*) FROM hazard_report WHERE project_id=?", projectId));
        summary.put("openHazards", queryCount("SELECT COUNT(*) FROM hazard_report WHERE project_id=? AND status IN (1,2)", projectId));
        summary.put("closedHazards", queryCount("SELECT COUNT(*) FROM hazard_report WHERE project_id=? AND status IN (3,4)", projectId));
        summary.put("todayViolations", queryCount("SELECT COUNT(*) FROM hazard_report WHERE project_id=? AND report_type=1 AND DATE(created_at)=CURDATE()", projectId));

        // 设备统计
        summary.put("totalDevices", queryCount("SELECT COUNT(*) FROM device WHERE project_id=?", projectId));
        summary.put("onlineDevices", queryCount("SELECT COUNT(*) FROM device WHERE project_id=? AND status='online'", projectId));
        summary.put("alarmDevices", queryCount("SELECT COUNT(*) FROM device_alarm WHERE project_id=? AND status='active'", projectId));

        // 进度统计
        summary.put("delayTasks", queryCount("SELECT COUNT(*) FROM progress_task WHERE project_id=? AND status='delayed'", projectId));
        summary.put("totalTasks", queryCount("SELECT COUNT(*) FROM progress_task WHERE project_id=?", projectId));

        // 围栏告警（如有）
        summary.put("fenceAlerts", queryCount("SELECT COUNT(*) FROM fence_alert_event WHERE project_id=? AND status='pending'", projectId));

        // 特种作业票（如有）
        summary.put("activePermits", queryCount("SELECT COUNT(*) FROM special_work_permit WHERE project_id=? AND status='active'", projectId));

        return Result.ok(summary);
    }

    private long queryCount(String sql, Long projectId) {
        Long count = jdbcTemplate.queryForObject(sql, Long.class, projectId);
        return count != null ? count : 0L;
    }
}
