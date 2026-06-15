package com.powersmart.worker.controller;

import com.powersmart.common.entity.Result;
import com.powersmart.worker.service.LabourInfocollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 人员信息采集 — 同业电力前端 build/labourInfocollection/*
 */
@RestController
@RequestMapping("/build/labourInfocollection")
@RequiredArgsConstructor
public class LabourInfocollectionController {

    private final LabourInfocollectionService labourInfocollectionService;

    @PostMapping("/getTeamLeaderList")
    public Result<List<Map<String, Object>>> getTeamLeaderList(@RequestBody Map<String, Object> params) {
        Long projectId = params.containsKey("projectId") && params.get("projectId") != null
                ? Long.valueOf(params.get("projectId").toString()) : null;
        return Result.ok(labourInfocollectionService.getTeamLeaderList(projectId));
    }

    @PostMapping("/selectOddIdAndName")
    public Result<List<Map<String, Object>>> selectOddIdAndName(@RequestBody Map<String, Object> params) {
        return getTeamLeaderList(params);
    }

    @PostMapping("/selectAttendanceData")
    public Result<List<Map<String, Object>>> selectAttendanceData(@RequestBody Map<String, Object> params) {
        Long projectId = params.containsKey("projectId") && params.get("projectId") != null
                ? Long.valueOf(params.get("projectId").toString()) : null;
        return Result.ok(labourInfocollectionService.selectAttendanceData(projectId));
    }
}
