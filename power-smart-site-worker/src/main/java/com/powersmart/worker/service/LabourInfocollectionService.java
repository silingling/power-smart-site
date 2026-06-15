package com.powersmart.worker.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.powersmart.worker.entity.AttendanceRecord;
import com.powersmart.worker.entity.WorkerTeam;
import com.powersmart.worker.mapper.AttendanceRecordMapper;
import com.powersmart.worker.mapper.WorkerTeamMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 人员信息采集 Service — 提取 LabourInfocollectionController 的业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LabourInfocollectionService {

    private final WorkerTeamMapper teamMapper;
    private final AttendanceRecordMapper attendanceMapper;

    /**
     * 获取班组长列表
     */
    public List<Map<String, Object>> getTeamLeaderList(Long projectId) {
        List<WorkerTeam> teams = teamMapper.selectList(
                new LambdaQueryWrapper<WorkerTeam>()
                        .eq(projectId != null, WorkerTeam::getProjectId, projectId));
        return teams.stream().map(t -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", t.getId());
            m.put("leaderName", t.getLeaderName());
            m.put("teamName", t.getTeamName());
            m.put("leaderPhone", t.getLeaderPhone());
            return m;
        }).collect(Collectors.toList());
    }

    /**
     * 查询当日考勤数据概览 + 明细
     */
    public List<Map<String, Object>> selectAttendanceData(Long projectId) {
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
        Map<String, Object> statItem = new LinkedHashMap<>();
        statItem.put("type", "summary");
        statItem.put("data", summary);
        result.add(statItem);

        for (AttendanceRecord r : records) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("workerId", r.getWorkerId());
            item.put("attendDate", r.getAttendDate().toString());
            item.put("checkInTime", r.getCheckInTime());
            item.put("checkOutTime", r.getCheckOutTime());
            item.put("status", r.getStatus());
            result.add(item);
        }

        return result;
    }
}
