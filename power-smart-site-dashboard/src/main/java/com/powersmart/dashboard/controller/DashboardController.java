package com.powersmart.dashboard.controller;

import com.powersmart.common.entity.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/project/{projectId}/summary")
    public Result<Map<String, Object>> getProjectSummary(@PathVariable Long projectId) {
        Map<String, Object> summary = new HashMap<>();

        // 人员统计
        summary.put("totalWorkers", queryCount("worker", projectId));
        summary.put("activeWorkers", queryCountWhere("worker", "status=1", projectId));
        summary.put("todayViolations", queryCount("SELECT COUNT(*) FROM hazard_report WHERE project_id=? AND report_type=1 AND DATE(created_at)=CURDATE()", projectId));

        // 设备统计
        summary.put("totalDevices", queryCount("device", projectId));
        summary.put("onlineDevices", queryCountWhere("device", "status=2", projectId));
        summary.put("alarmDevices", queryCountWhere("device", "status=3", projectId));

        // 进度统计
        summary.put("delayTasks", queryCount("SELECT COUNT(*) FROM progress_task WHERE project_id=? AND status=3", projectId));

        // 隐患统计
        summary.put("openHazards", queryCountWhere("hazard_report", "status IN (1,2)", projectId));
        summary.put("closedHazards", queryCountWhere("hazard_report", "status IN (3,4)", projectId));

        return Result.ok(summary);
    }

    private long queryCount(String table, Long projectId) {
        return queryCount("SELECT COUNT(*) FROM " + table + " WHERE project_id=?", projectId);
    }

    private long queryCountWhere(String table, String where, Long projectId) {
        return queryCount("SELECT COUNT(*) FROM " + table + " WHERE project_id=? AND " + where, projectId);
    }

    private long queryCount(String sql, Long projectId) {
        Long count = jdbcTemplate.queryForObject(sql, Long.class, projectId);
        return count != null ? count : 0L;
    }
}
