package com.powersmart.device.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.device.entity.TransmissionTower;
import com.powersmart.device.service.TransmissionLineService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 输电线路杆塔管理 — /build/transmissionTower/*
 */
@RestController
@RequestMapping("/build/transmissionTower")
@RequiredArgsConstructor
public class TransmissionController {

    private final TransmissionLineService service;

    @PostMapping("/queryTransmissionTowerList")
    public Result<PageResult<Map<String, Object>>> queryTowerList(@RequestBody(required = false) Map<String, Object> params) {
        Page<TransmissionTower> page = service.queryTowerPage(params);
        List<Map<String, Object>> list = page.getRecords().stream().map(this::towerToMap).collect(Collectors.toList());
        return Result.ok(PageResult.of(list, page.getTotal(), (int) page.getCurrent(), (int) page.getSize()));
    }

    @PostMapping("/getTransmissionTower/{id}")
    public Result<Map<String, Object>> getTower(@PathVariable Long id) {
        TransmissionTower t = service.getTowerById(id);
        if (t == null) return Result.fail("杆塔不存在");
        return Result.ok(towerToMap(t));
    }

    @PostMapping("/addTransmissionTower")
    public Result<Void> addTower(@RequestBody Map<String, Object> params) {
        TransmissionTower t = buildTower(null, params);
        service.addTower(t);
        return Result.ok();
    }

    @PostMapping("/setTransmissionTower")
    public Result<Void> setTower(@RequestBody Map<String, Object> params) {
        Long id = safeLong(params.get("id"));
        if (id == null) return Result.fail("id 不能为空");
        TransmissionTower t = service.getTowerById(id);
        if (t == null) return Result.fail("杆塔不存在");
        service.updateTower(buildTower(t, params));
        return Result.ok();
    }

    @PostMapping("/delTransmissionTower/{id}")
    public Result<Void> delTower(@PathVariable Long id) {
        service.deleteTower(id);
        return Result.ok();
    }

    @PostMapping("/getActiveTowerList")
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
        if (p.containsKey("manufactureDate")) t.setManufactureDate(LocalDate.parse(p.get("manufactureDate").toString()));
        if (p.containsKey("installDate")) t.setInstallDate(LocalDate.parse(p.get("installDate").toString()));
        if (p.containsKey("remark")) t.setRemark(p.get("remark").toString());
        return t;
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
