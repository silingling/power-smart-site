package com.powersmart.worker.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.powersmart.common.entity.Result;
import com.powersmart.worker.entity.AttendanceRecord;
import com.powersmart.worker.entity.WorkerTeam;
import com.powersmart.worker.mapper.AttendanceRecordMapper;
import com.powersmart.worker.mapper.WorkerTeamMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 人员信息采集 — 同业电力前端 build/labourInfocollection/*
 */
@RestController
@RequestMapping("/build/labourInfocollection")
@RequiredArgsConstructor
public class LabourInfocollectionController {

    private final WorkerTeamMapper teamMapper;
    private final AttendanceRecordMapper attendanceMapper;

    @PostMapping("/getTeamLeaderList")
    public Result<List<Map<String, Object>>> getTeamLeaderList(@RequestBody Map<String, Object> params) {
        Long projectId = params.containsKey("projectId") && params.get("projectId") != null
                ? Long.valueOf(params.get("projectId").toString()) : null;
        List<WorkerTeam> teams = teamMapper.selectList(
                new LambdaQueryWrapper<WorkerTeam>()
                        .eq(projectId != null, WorkerTeam::getProjectId, projectId));
        List<Map<String, Object>> result = teams.stream().map(t -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", t.getId());
            m.put("leaderName", t.getLeaderName());
            m.put("teamName", t.getTeamName());
            m.put("leaderPhone", t.getLeaderPhone());
            return m;
        }).collect(Collectors.toList());
        return Result.ok(result);
    }

    @PostMapping("/selectOddIdAndName")
    public Result<List<Map<String, Object>>> selectOddIdAndName(@RequestBody Map<String, Object> params) {
        return getTeamLeaderList(params);
    }

    @PostMapping("/selectAttendanceData")
    public Result<List<Map<String, Object>>> selectAttendanceData(@RequestBody Map<String, Object> params) {
        Long projectId = params.containsKey("projectId") && params.get("projectId") != null
                ? Long.valueOf(params.get("projectId").toString()) : null;

        LocalDate today = LocalDate.now();
        List<AttendanceRecord> records;

        if (projectId != null) {
            records = attendanceMapper.selectList(
                    new LambdaQueryWrapper<AttendanceRecord>()
                            .eq(AttendanceRecord::getProjectId, projectId)
                            .eq(AttendanceRecord::getAttendDate, today)
                            .orderByDesc(AttendanceRecord::getCreatedAt));
        } else {
            records = attendanceMapper.selectList(
                    new LambdaQueryWrapper<AttendanceRecord>()
                            .eq(AttendanceRecord::getAttendDate, today)
                            .orderByDesc(AttendanceRecord::getCreatedAt));
        }

        // 统计概览
        long normal = records.stream().filter(r -> r.getStatus() == 1).count();
        long late = records.stream().filter(r -> r.getStatus() == 2).count();
        long early = records.stream().filter(r -> r.getStatus() == 3).count();
        long absent = records.stream().filter(r -> r.getStatus() == 4).count();

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("normal", normal);
        summary.put("late", late);
        summary.put("early", early);
        summary.put("absent", absent);
        summary.put("total", records.size());

        List<Map<String, Object>> result = new ArrayList<>();
        // 第一条放统计
        Map<String, Object> statItem = new LinkedHashMap<>();
        statItem.put("type", "summary");
        statItem.put("data", summary);
        result.add(statItem);

        // 后续放明细
        for (AttendanceRecord r : records) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("workerId", r.getWorkerId());
            item.put("attendDate", r.getAttendDate().toString());
            item.put("checkInTime", r.getCheckInTime());
            item.put("checkOutTime", r.getCheckOutTime());
            item.put("status", r.getStatus());
            result.add(item);
        }

        return Result.ok(result);
    }
}
