package com.powersmart.device.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.device.entity.TransmissionSpan;
import com.powersmart.device.entity.TransmissionTower;
import com.powersmart.device.service.TransmissionLineService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 输电线路杆塔/档距管理 — /build/transmissionTower/* /build/transmissionSpan/*
 */
@RestController
@RequiredArgsConstructor
public class TransmissionController {

    private final TransmissionLineService service;

    // ====================== 杆塔 Tower ======================

    @PostMapping("/build/transmissionTower/queryTransmissionTowerList")
    public Result<PageResult<Map<String, Object>>> queryTowerList(@RequestBody(required = false) Map<String, Object> params) {
        Page<TransmissionTower> page = service.queryTowerPage(params);
        List<Map<String, Object>> list = page.getRecords().stream().map(this::towerToMap).collect(Collectors.toList());
        return Result.ok(PageResult.of(list, page.getTotal(), (int) page.getCurrent(), (int) page.getSize()));
    }

    @PostMapping("/build/transmissionTower/getTransmissionTower/{id}")
    public Result<Map<String, Object>> getTower(@PathVariable Long id) {
        TransmissionTower t = service.getTowerById(id);
        if (t == null) return Result.fail("杆塔不存在");
        return Result.ok(towerToMap(t));
    }

    @PostMapping("/build/transmissionTower/addTransmissionTower")
    public Result<Void> addTower(@RequestBody Map<String, Object> params) {
        TransmissionTower t = buildTower(null, params);
        service.addTower(t);
        return Result.ok();
    }

    @PostMapping("/build/transmissionTower/setTransmissionTower")
    public Result<Void> setTower(@RequestBody Map<String, Object> params) {
        Long id = safeLong(params.get("id"));
        if (id == null) return Result.fail("id 不能为空");
        TransmissionTower t = service.getTowerById(id);
        if (t == null) return Result.fail("杆塔不存在");
        service.updateTower(buildTower(t, params));
        return Result.ok();
    }

    @PostMapping("/build/transmissionTower/delTransmissionTower/{id}")
    public Result<Void> delTower(@PathVariable Long id) {
        service.deleteTower(id);
        return Result.ok();
    }

    /** 获取活跃杆塔列表（下拉选择用） */
    @PostMapping("/build/transmissionTower/getActiveTowerList")
    public Result<List<Map<String, Object>>> getActiveTowers(@RequestBody Map<String, Object> params) {
        Long projectId = safeLong(params.get("projectId"));
        if (projectId == null) return Result.fail("projectId 不能为空");
        List<Map<String, Object>> list = service.getActiveTowers(projectId).stream().map(t -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", t.getId());
            m.put("towerCode", t.getTowerCode());
            m.put("towerName", t.getTowerName());
            m.put("towerType", t.getTowerType());
            return m;
        }).collect(Collectors.toList());
        return Result.ok(list);
    }

    // ====================== 档距 Span ======================

    @PostMapping("/build/transmissionSpan/queryTransmissionSpanList")
    public Result<PageResult<Map<String, Object>>> querySpanList(@RequestBody(required = false) Map<String, Object> params) {
        Page<TransmissionSpan> page = service.querySpanPage(params);
        List<Map<String, Object>> list = page.getRecords().stream().map(this::spanToMap).collect(Collectors.toList());
        return Result.ok(PageResult.of(list, page.getTotal(), (int) page.getCurrent(), (int) page.getSize()));
    }

    @PostMapping("/build/transmissionSpan/getTransmissionSpan/{id}")
    public Result<Map<String, Object>> getSpan(@PathVariable Long id) {
        TransmissionSpan s = service.getSpanById(id);
        if (s == null) return Result.fail("档距不存在");
        return Result.ok(spanToMap(s));
    }

    @PostMapping("/build/transmissionSpan/addTransmissionSpan")
    public Result<Void> addSpan(@RequestBody Map<String, Object> params) {
        TransmissionSpan s = buildSpan(null, params);
        service.addSpan(s);
        return Result.ok();
    }

    @PostMapping("/build/transmissionSpan/setTransmissionSpan")
    public Result<Void> setSpan(@RequestBody Map<String, Object> params) {
        Long id = safeLong(params.get("id"));
        if (id == null) return Result.fail("id 不能为空");
        TransmissionSpan s = service.getSpanById(id);
        if (s == null) return Result.fail("档距不存在");
        service.updateSpan(buildSpan(s, params));
        return Result.ok();
    }

    @PostMapping("/build/transmissionSpan/delTransmissionSpan/{id}")
    public Result<Void> delSpan(@PathVariable Long id) {
        service.deleteSpan(id);
        return Result.ok();
    }

    // ====================== 帮助方法 ======================

    private Map<String, Object> towerToMap(TransmissionTower t) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", t.getId());
        m.put("projectId", t.getProjectId());
        m.put("towerCode", t.getTowerCode());
        m.put("towerName", t.getTowerName());
        m.put("towerType", t.getTowerType());
        m.put("voltageLevel", t.getVoltageLevel());
        m.put("heightM", t.getHeightM());
        m.put("latitude", t.getLatitude());
        m.put("longitude", t.getLongitude());
        m.put("altitudeM", t.getAltitudeM());
        m.put("foundationType", t.getFoundationType());
        m.put("foundationDepthM", t.getFoundationDepthM());
        m.put("legCount", t.getLegCount());
        m.put("manufacturer", t.getManufacturer());
        m.put("model", t.getModel());
        m.put("serialNumber", t.getSerialNumber());
        m.put("manufactureDate", t.getManufactureDate() != null ? t.getManufactureDate().toString() : "");
        m.put("installDate", t.getInstallDate() != null ? t.getInstallDate().toString() : "");
        m.put("designLifeYears", t.getDesignLifeYears());
        m.put("status", t.getStatus());
        m.put("lastInspectionDate", t.getLastInspectionDate() != null ? t.getLastInspectionDate().toString() : "");
        m.put("nextInspectionDate", t.getNextInspectionDate() != null ? t.getNextInspectionDate().toString() : "");
        m.put("imageJson", t.getImageJson());
        m.put("remark", t.getRemark());
        m.put("createdAt", t.getCreatedAt() != null ? t.getCreatedAt().toString() : "");
        return m;
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

    private TransmissionTower buildTower(TransmissionTower t, Map<String, Object> p) {
        if (t == null) { t = new TransmissionTower(); t.setStatus("in_service"); }
        if (p.containsKey("projectId")) t.setProjectId(safeLong(p.get("projectId")));
        if (p.containsKey("towerCode")) t.setTowerCode(p.get("towerCode").toString());
        if (p.containsKey("towerName")) t.setTowerName(p.get("towerName").toString());
        if (p.containsKey("towerType")) t.setTowerType(p.get("towerType").toString());
        if (p.containsKey("voltageLevel")) t.setVoltageLevel(p.get("voltageLevel").toString());
        if (p.containsKey("heightM")) t.setHeightM(safeDec(p.get("heightM")));
        if (p.containsKey("latitude")) t.setLatitude(safeDec(p.get("latitude")));
        if (p.containsKey("longitude")) t.setLongitude(safeDec(p.get("longitude")));
        if (p.containsKey("altitudeM")) t.setAltitudeM(safeDec(p.get("altitudeM")));
        if (p.containsKey("foundationType")) t.setFoundationType(p.get("foundationType").toString());
        if (p.containsKey("foundationDepthM")) t.setFoundationDepthM(safeDec(p.get("foundationDepthM")));
        if (p.containsKey("legCount")) t.setLegCount(Integer.parseInt(p.get("legCount").toString()));
        if (p.containsKey("manufacturer")) t.setManufacturer(p.get("manufacturer").toString());
        if (p.containsKey("model")) t.setModel(p.get("model").toString());
        if (p.containsKey("serialNumber")) t.setSerialNumber(p.get("serialNumber").toString());
        if (p.containsKey("status")) t.setStatus(p.get("status").toString());
        if (p.containsKey("designLifeYears")) t.setDesignLifeYears(Integer.parseInt(p.get("designLifeYears").toString()));
        if (p.containsKey("manufactureDate")) t.setManufactureDate(java.time.LocalDate.parse(p.get("manufactureDate").toString()));
        if (p.containsKey("installDate")) t.setInstallDate(java.time.LocalDate.parse(p.get("installDate").toString()));
        if (p.containsKey("remark")) t.setRemark(p.get("remark").toString());
        return t;
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
    private java.math.BigDecimal safeDec(Object v) {
        if (v == null) return null;
        try { return new java.math.BigDecimal(v.toString()); } catch (NumberFormatException e) { return null; }
    }
}
