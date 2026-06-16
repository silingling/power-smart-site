package com.powersmart.device.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.device.entity.TransmissionSpan;
import com.powersmart.device.service.TransmissionLineService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 输电线档距管理 — /build/transmissionSpan/*
 */
@RestController
@RequestMapping("/build/transmissionSpan")
@RequiredArgsConstructor
public class TransmissionSpanController {

    private final TransmissionLineService service;

    @PostMapping("/queryTransmissionSpanList")
    public Result<PageResult<Map<String, Object>>> querySpanList(@RequestBody(required = false) Map<String, Object> params) {
        Page<TransmissionSpan> page = service.querySpanPage(params);
        List<Map<String, Object>> list = page.getRecords().stream().map(this::spanToMap).collect(Collectors.toList());
        return Result.ok(PageResult.of(list, page.getTotal(), (int) page.getCurrent(), (int) page.getSize()));
    }

    @PostMapping("/getTransmissionSpan/{id}")
    public Result<Map<String, Object>> getSpan(@PathVariable Long id) {
        TransmissionSpan s = service.getSpanById(id);
        if (s == null) return Result.fail("档距不存在");
        return Result.ok(spanToMap(s));
    }

    @PostMapping("/addTransmissionSpan")
    public Result<Void> addSpan(@RequestBody Map<String, Object> params) {
        TransmissionSpan s = buildSpan(null, params);
        service.addSpan(s);
        return Result.ok();
    }

    @PostMapping("/setTransmissionSpan")
    public Result<Void> setSpan(@RequestBody Map<String, Object> params) {
        Long id = safeLong(params.get("id"));
        if (id == null) return Result.fail("id 不能为空");
        TransmissionSpan s = service.getSpanById(id);
        if (s == null) return Result.fail("档距不存在");
        service.updateSpan(buildSpan(s, params));
        return Result.ok();
    }

    @PostMapping("/delTransmissionSpan/{id}")
    public Result<Void> delSpan(@PathVariable Long id) {
        service.deleteSpan(id);
        return Result.ok();
    }

    private Map<String, Object> spanToMap(TransmissionSpan s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", s.getId());
        m.put("projectId", s.getProjectId());
        m.put("spanCode", s.getSpanCode());
        m.put("fromTowerId", s.getFromTowerId());
        m.put("toTowerId", s.getToTowerId());
        m.put("spanLengthM", s.getSpanLengthM());
        m.put("conductorType", s.getConductorType());
        m.put("conductorSpec", s.getConductorSpec());
        m.put("circuitCount", s.getCircuitCount());
        m.put("designSagM", s.getDesignSagM());
        m.put("currentSagM", s.getCurrentSagM());
        m.put("maxSagAllowedM", s.getMaxSagAllowedM());
        m.put("sagAlarmThresholdPct", s.getSagAlarmThresholdPct());
        m.put("maxWindSpeedMs", s.getMaxWindSpeedMs());
        m.put("minClearanceM", s.getMinClearanceM());
        m.put("terrainType", s.getTerrainType());
        m.put("crossingDesc", s.getCrossingDesc());
        m.put("lastInspectionDate", s.getLastInspectionDate() != null ? s.getLastInspectionDate().toString() : "");
        m.put("nextInspectionDate", s.getNextInspectionDate() != null ? s.getNextInspectionDate().toString() : "");
        m.put("status", s.getStatus());
        m.put("createdAt", s.getCreatedAt() != null ? s.getCreatedAt().toString() : "");
        return m;
    }

    private TransmissionSpan buildSpan(TransmissionSpan s, Map<String, Object> p) {
        if (s == null) { s = new TransmissionSpan(); s.setStatus("normal"); }
        if (p.containsKey("projectId")) s.setProjectId(safeLong(p.get("projectId")));
        if (p.containsKey("spanCode")) s.setSpanCode(p.get("spanCode").toString());
        if (p.containsKey("fromTowerId")) s.setFromTowerId(safeLong(p.get("fromTowerId")));
        if (p.containsKey("toTowerId")) s.setToTowerId(safeLong(p.get("toTowerId")));
        if (p.containsKey("spanLengthM")) s.setSpanLengthM(safeDec(p.get("spanLengthM")));
        if (p.containsKey("conductorType")) s.setConductorType(p.get("conductorType").toString());
        if (p.containsKey("conductorSpec")) s.setConductorSpec(p.get("conductorSpec").toString());
        if (p.containsKey("circuitCount")) s.setCircuitCount(Integer.parseInt(p.get("circuitCount").toString()));
        if (p.containsKey("designSagM")) s.setDesignSagM(safeDec(p.get("designSagM")));
        if (p.containsKey("currentSagM")) s.setCurrentSagM(safeDec(p.get("currentSagM")));
        if (p.containsKey("maxSagAllowedM")) s.setMaxSagAllowedM(safeDec(p.get("maxSagAllowedM")));
        if (p.containsKey("sagAlarmThresholdPct")) s.setSagAlarmThresholdPct(safeDec(p.get("sagAlarmThresholdPct")));
        if (p.containsKey("maxWindSpeedMs")) s.setMaxWindSpeedMs(safeDec(p.get("maxWindSpeedMs")));
        if (p.containsKey("minClearanceM")) s.setMinClearanceM(safeDec(p.get("minClearanceM")));
        if (p.containsKey("terrainType")) s.setTerrainType(p.get("terrainType").toString());
        if (p.containsKey("crossingDesc")) s.setCrossingDesc(p.get("crossingDesc").toString());
        if (p.containsKey("status")) s.setStatus(p.get("status").toString());
        return s;
    }

    private Long safeLong(Object v) {
        if (v == null) return null;
        try { return Long.parseLong(v.toString()); } catch (NumberFormatException e) { return null; }
    }
    private BigDecimal safeDec(Object v) {
        if (v == null) return null;
        try { return new BigDecimal(v.toString()); } catch (NumberFormatException e) { return null; }
    }
}
