package com.powersmart.hazard.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.annotation.OperateLog;
import com.powersmart.common.auth.SecurityContext;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.util.PageHelper;
import com.powersmart.hazard.entity.EmergencyDrill;
import com.powersmart.hazard.mapper.EmergencyDrillMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@OperateLog
public class EmergencyDrillService {

    private final EmergencyDrillMapper emergencyDrillMapper;

    public PageResult<EmergencyDrill> list(Map<String, Object> params) {
        Page<EmergencyDrill> page = PageHelper.of(params);
        LambdaQueryWrapper<EmergencyDrill> wrapper = new LambdaQueryWrapper<>();
        if (params != null) {
            if (params.containsKey("projectId") && params.get("projectId") != null)
                wrapper.eq(EmergencyDrill::getProjectId, Long.valueOf(params.get("projectId").toString()));
            if (params.containsKey("planId") && params.get("planId") != null)
                wrapper.eq(EmergencyDrill::getPlanId, Long.valueOf(params.get("planId").toString()));
            if (params.containsKey("drillType") && params.get("drillType") != null)
                wrapper.eq(EmergencyDrill::getDrillType, params.get("drillType").toString());
            if (params.containsKey("startDate") && params.get("startDate") != null)
                wrapper.ge(EmergencyDrill::getDrillDate, LocalDate.parse(params.get("startDate").toString()));
            if (params.containsKey("endDate") && params.get("endDate") != null)
                wrapper.le(EmergencyDrill::getDrillDate, LocalDate.parse(params.get("endDate").toString()));
        }
        wrapper.orderByDesc(EmergencyDrill::getDrillDate);
        return PageResult.from(emergencyDrillMapper.selectPage(page, wrapper));
    }

    public EmergencyDrill getById(Long id) {
        return emergencyDrillMapper.selectById(id);
    }

    public void add(EmergencyDrill entity) {
        if (StrUtil.isBlank(entity.getDrillName())) {
            throw new IllegalArgumentException("演练名称不能为空");
        }
        if (entity.getDrillDate() == null) {
            throw new IllegalArgumentException("演练日期不能为空");
        }
        entity.setCreatedBy(SecurityContext.getCurrentUserId());
        emergencyDrillMapper.insert(entity);
    }

    public void update(EmergencyDrill entity) {
        EmergencyDrill existing = emergencyDrillMapper.selectById(entity.getId());
        if (existing == null) {
            throw new IllegalArgumentException("演练记录不存在");
        }
        if (StrUtil.isNotBlank(entity.getDrillName())) {
            existing.setDrillName(entity.getDrillName());
        }
        if (StrUtil.isNotBlank(entity.getDrillType())) {
            existing.setDrillType(entity.getDrillType());
        }
        if (entity.getDrillDate() != null) {
            existing.setDrillDate(entity.getDrillDate());
        }
        if (entity.getDrillTime() != null) {
            existing.setDrillTime(entity.getDrillTime());
        }
        if (entity.getDurationMinutes() != null) {
            existing.setDurationMinutes(entity.getDurationMinutes());
        }
        if (entity.getLocation() != null) {
            existing.setLocation(entity.getLocation());
        }
        if (entity.getParticipantsCount() != null) {
            existing.setParticipantsCount(entity.getParticipantsCount());
        }
        if (entity.getOrganizer() != null) {
            existing.setOrganizer(entity.getOrganizer());
        }
        if (entity.getContent() != null) {
            existing.setContent(entity.getContent());
        }
        if (entity.getEvaluation() != null) {
            existing.setEvaluation(entity.getEvaluation());
        }
        if (entity.getDeficiencies() != null) {
            existing.setDeficiencies(entity.getDeficiencies());
        }
        if (entity.getImprovementMeasures() != null) {
            existing.setImprovementMeasures(entity.getImprovementMeasures());
        }
        if (entity.getAttachmentJson() != null) {
            existing.setAttachmentJson(entity.getAttachmentJson());
        }
        if (StrUtil.isNotBlank(entity.getResult())) {
            existing.setResult(entity.getResult());
        }
        if (entity.getPlanId() != null) {
            existing.setPlanId(entity.getPlanId());
        }
        emergencyDrillMapper.updateById(existing);
    }

    public void delete(Long id) {
        emergencyDrillMapper.deleteById(id);
    }

    public Map<String, Object> getDrillStats(Long projectId, Integer year) {
        Map<String, Object> stats = new LinkedHashMap<>();
        if (year == null) {
            year = LocalDate.now().getYear();
        }
        LocalDate yearStart = LocalDate.of(year, 1, 1);
        LocalDate yearEnd = LocalDate.of(year, 12, 31);

        List<EmergencyDrill> drills = emergencyDrillMapper.selectList(
                new LambdaQueryWrapper<EmergencyDrill>()
                        .eq(EmergencyDrill::getProjectId, projectId)
                        .ge(EmergencyDrill::getDrillDate, yearStart)
                        .le(EmergencyDrill::getDrillDate, yearEnd));

        // Statistics by month
        Map<Integer, Long> monthStats = drills.stream()
                .collect(Collectors.groupingBy(
                        d -> d.getDrillDate().getMonthValue(),
                        Collectors.counting()));
        stats.put("monthStats", monthStats);

        // Statistics by drill type
        Map<String, Long> typeStats = drills.stream()
                .collect(Collectors.groupingBy(
                        EmergencyDrill::getDrillType,
                        Collectors.counting()));
        stats.put("typeStats", typeStats);

        // Statistics by result
        Map<String, Long> resultStats = drills.stream()
                .filter(d -> StrUtil.isNotBlank(d.getResult()))
                .collect(Collectors.groupingBy(
                        EmergencyDrill::getResult,
                        Collectors.counting()));
        stats.put("resultStats", resultStats);

        stats.put("totalCount", drills.size());
        stats.put("year", year);

        return stats;
    }
}
