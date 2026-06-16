package com.powersmart.hazard.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.annotation.OperateLog;
import com.powersmart.common.auth.SecurityContext;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.util.PageHelper;
import com.powersmart.hazard.entity.EmergencyIncident;
import com.powersmart.hazard.mapper.EmergencyIncidentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@OperateLog
public class EmergencyIncidentService {

    private final EmergencyIncidentMapper emergencyIncidentMapper;

    public PageResult<EmergencyIncident> list(Map<String, Object> params) {
        Page<EmergencyIncident> page = PageHelper.of(params);
        LambdaQueryWrapper<EmergencyIncident> wrapper = new LambdaQueryWrapper<>();
        if (params != null) {
            if (params.containsKey("projectId") && params.get("projectId") != null)
                wrapper.eq(EmergencyIncident::getProjectId, Long.valueOf(params.get("projectId").toString()));
            if (params.containsKey("incidentType") && params.get("incidentType") != null)
                wrapper.eq(EmergencyIncident::getIncidentType, params.get("incidentType").toString());
            if (params.containsKey("incidentLevel") && params.get("incidentLevel") != null)
                wrapper.eq(EmergencyIncident::getIncidentLevel, params.get("incidentLevel").toString());
            if (params.containsKey("status") && params.get("status") != null)
                wrapper.eq(EmergencyIncident::getStatus, params.get("status").toString());
            if (params.containsKey("startDate") && params.get("startDate") != null)
                wrapper.ge(EmergencyIncident::getHappenedAt, LocalDateTime.parse(params.get("startDate").toString() + "T00:00:00"));
            if (params.containsKey("endDate") && params.get("endDate") != null)
                wrapper.le(EmergencyIncident::getHappenedAt, LocalDateTime.parse(params.get("endDate").toString() + "T23:59:59"));
        }
        wrapper.orderByDesc(EmergencyIncident::getHappenedAt);
        return PageResult.from(emergencyIncidentMapper.selectPage(page, wrapper));
    }

    public EmergencyIncident getById(Long id) {
        return emergencyIncidentMapper.selectById(id);
    }

    public void add(EmergencyIncident entity) {
        if (StrUtil.isBlank(entity.getIncidentName())) {
            throw new IllegalArgumentException("事件名称不能为空");
        }
        if (StrUtil.isBlank(entity.getIncidentType())) {
            throw new IllegalArgumentException("事件类型不能为空");
        }
        if (entity.getHappenedAt() == null) {
            throw new IllegalArgumentException("发生时间不能为空");
        }

        // Auto-generate incident code
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        // Find max sequence for today
        Integer maxSeq = emergencyIncidentMapper.selectCount(
                new LambdaQueryWrapper<EmergencyIncident>()
                        .likeRight(EmergencyIncident::getIncidentCode, "INC-" + datePart));
        String seq = String.format("%04d", (maxSeq != null ? maxSeq : 0) + 1);
        entity.setIncidentCode("INC-" + datePart + "-" + seq);

        entity.setReporter(SecurityContext.getCurrentUserId());
        if (entity.getReportTime() == null) {
            entity.setReportTime(LocalDateTime.now());
        }
        if (StrUtil.isBlank(entity.getStatus())) {
            entity.setStatus("pending");
        }
        emergencyIncidentMapper.insert(entity);
    }

    public void update(EmergencyIncident entity) {
        EmergencyIncident existing = emergencyIncidentMapper.selectById(entity.getId());
        if (existing == null) {
            throw new IllegalArgumentException("事件记录不存在");
        }
        if (StrUtil.isNotBlank(entity.getIncidentName())) {
            existing.setIncidentName(entity.getIncidentName());
        }
        if (StrUtil.isNotBlank(entity.getIncidentType())) {
            existing.setIncidentType(entity.getIncidentType());
        }
        if (StrUtil.isNotBlank(entity.getIncidentLevel())) {
            existing.setIncidentLevel(entity.getIncidentLevel());
        }
        if (entity.getHappenedAt() != null) {
            existing.setHappenedAt(entity.getHappenedAt());
        }
        if (entity.getLocation() != null) {
            existing.setLocation(entity.getLocation());
        }
        if (entity.getDescription() != null) {
            existing.setDescription(entity.getDescription());
        }
        if (entity.getCasualties() != null) {
            existing.setCasualties(entity.getCasualties());
        }
        if (entity.getDeaths() != null) {
            existing.setDeaths(entity.getDeaths());
        }
        if (entity.getDirectLoss() != null) {
            existing.setDirectLoss(entity.getDirectLoss());
        }
        if (entity.getIndirectLoss() != null) {
            existing.setIndirectLoss(entity.getIndirectLoss());
        }
        if (entity.getPreliminaryCause() != null) {
            existing.setPreliminaryCause(entity.getPreliminaryCause());
        }
        if (entity.getInvestigationReport() != null) {
            existing.setInvestigationReport(entity.getInvestigationReport());
        }
        if (entity.getCorrectiveActions() != null) {
            existing.setCorrectiveActions(entity.getCorrectiveActions());
        }
        if (StrUtil.isNotBlank(entity.getCorrectiveStatus())) {
            existing.setCorrectiveStatus(entity.getCorrectiveStatus());
        }
        if (entity.getAttachmentJson() != null) {
            existing.setAttachmentJson(entity.getAttachmentJson());
        }
        if (StrUtil.isNotBlank(entity.getStatus())) {
            existing.setStatus(entity.getStatus());
        }
        emergencyIncidentMapper.updateById(existing);
    }

    public void delete(Long id) {
        emergencyIncidentMapper.deleteById(id);
    }

    public void updateStatus(Long id, String status) {
        EmergencyIncident incident = emergencyIncidentMapper.selectById(id);
        if (incident == null) {
            throw new IllegalArgumentException("事件记录不存在");
        }
        incident.setStatus(status);
        emergencyIncidentMapper.updateById(incident);
    }

    public Map<String, Object> getIncidentStats(Long projectId) {
        Map<String, Object> stats = new LinkedHashMap<>();
        List<EmergencyIncident> incidents = emergencyIncidentMapper.selectList(
                new LambdaQueryWrapper<EmergencyIncident>()
                        .eq(EmergencyIncident::getProjectId, projectId));

        // Statistics by type
        Map<String, Long> typeStats = incidents.stream()
                .filter(i -> StrUtil.isNotBlank(i.getIncidentType()))
                .collect(Collectors.groupingBy(
                        EmergencyIncident::getIncidentType,
                        Collectors.counting()));
        stats.put("typeStats", typeStats);

        // Statistics by level
        Map<String, Long> levelStats = incidents.stream()
                .filter(i -> StrUtil.isNotBlank(i.getIncidentLevel()))
                .collect(Collectors.groupingBy(
                        EmergencyIncident::getIncidentLevel,
                        Collectors.counting()));
        stats.put("levelStats", levelStats);

        // Statistics by status
        Map<String, Long> statusStats = incidents.stream()
                .filter(i -> StrUtil.isNotBlank(i.getStatus()))
                .collect(Collectors.groupingBy(
                        EmergencyIncident::getStatus,
                        Collectors.counting()));
        stats.put("statusStats", statusStats);

        // Summary
        stats.put("totalCount", incidents.size());
        stats.put("totalCasualties", incidents.stream()
                .filter(i -> i.getCasualties() != null)
                .mapToInt(EmergencyIncident::getCasualties).sum());
        stats.put("totalDeaths", incidents.stream()
                .filter(i -> i.getDeaths() != null)
                .mapToInt(EmergencyIncident::getDeaths).sum());
        stats.put("totalDirectLoss", incidents.stream()
                .filter(i -> i.getDirectLoss() != null)
                .mapToDouble(i -> i.getDirectLoss().doubleValue()).sum());

        stats.put("projectId", projectId);
        return stats;
    }
}
