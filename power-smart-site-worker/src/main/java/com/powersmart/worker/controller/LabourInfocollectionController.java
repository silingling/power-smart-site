package com.powersmart.worker.controller;

import com.powersmart.common.entity.Result;
import com.powersmart.worker.entity.WorkerTeam;
import com.powersmart.worker.mapper.WorkerTeamMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 人员信息采集 — 同业电力前端 build/labourInfocollection/*
 */
@RestController
@RequestMapping("/build/labourInfocollection")
@RequiredArgsConstructor
public class LabourInfocollectionController {

    private final WorkerTeamMapper teamMapper;

    @PostMapping("/getTeamLeaderList")
    public Result<List<Map<String, Object>>> getTeamLeaderList(@RequestBody Map<String, Object> params) {
        Long projectId = params.containsKey("projectId") && params.get("projectId") != null
                ? Long.valueOf(params.get("projectId").toString()) : null;
        List<WorkerTeam> teams = teamMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<WorkerTeam>()
                        .eq(projectId != null, WorkerTeam::getProjectId, projectId));
        List<Map<String, Object>> result = teams.stream().map(t -> {
            Map<String, Object> m = new java.util.LinkedHashMap<>();
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
        // 简单返回班组列表作为"专业分包ID和名称"
        return getTeamLeaderList(params);
    }

    @PostMapping("/selectAttendanceData")
    public Result<List<Map<String, Object>>> selectAttendanceData(@RequestBody Map<String, Object> params) {
        // 考勤概览统计（返回空数据前端可接受）
        return Result.ok(java.util.Collections.emptyList());
    }
}
