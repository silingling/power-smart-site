package com.powersmart.device.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.device.entity.SubstationInspection;
import com.powersmart.device.service.SubstationEquipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 变电站巡检记录 — /build/substationInspection/*
 */
@RestController
@RequestMapping("/build/substationInspection")
@RequiredArgsConstructor
public class SubstationInspectionController {

    private final SubstationEquipmentService service;

    @PostMapping("/querySubstationInspectionList")
    public Result<PageResult<Map<String, Object>>> queryInspectionList(@RequestBody(required = false) Map<String, Object> params) {
        Page<SubstationInspection> page = service.queryInspectionPage(params);
        var list = page.getRecords().stream().map(insp -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", insp.getId());
            m.put("equipmentId", insp.getEquipmentId());
            m.put("inspector", insp.getInspector());
            m.put("inspectionType", insp.getInspectionType());
            m.put("inspectionDate", insp.getInspectionDate() != null ? insp.getInspectionDate().toString() : "");
            m.put("sf6Pressure", insp.getSf6Pressure());
            m.put("temperature", insp.getTemperature());
            m.put("noiseDb", insp.getNoiseDb());
            m.put("vibrationMm", insp.getVibrationMm());
            m.put("result", insp.getResult());
            m.put("description", insp.getDescription());
            m.put("imageJson", insp.getImageJson());
            m.put("createdAt", insp.getCreatedAt() != null ? insp.getCreatedAt().toString() : "");
            return m;
        }).collect(Collectors.toList());
        return Result.ok(PageResult.of(list, page.getTotal(), (int) page.getCurrent(), (int) page.getSize()));
    }

    @PostMapping("/addSubstationInspection")
    public Result<Void> addInspection(@RequestBody Map<String, Object> params) {
        SubstationInspection insp = new SubstationInspection();
        insp.setProjectId(safeLong(params.get("projectId")));
        insp.setEquipmentId(safeLong(params.get("equipmentId")));
        insp.setInspector(safeStr(params.get("inspector")));
        insp.setInspectionType(safeStr(params.get("inspectionType"), "routine"));
        if (params.containsKey("inspectionDate"))
            insp.setInspectionDate(java.time.LocalDate.parse(params.get("inspectionDate").toString()));
        insp.setContent(params.containsKey("content") ? params.get("content").toString() : null);
        if (params.containsKey("sf6Pressure"))
            insp.setSf6Pressure(new java.math.BigDecimal(params.get("sf6Pressure").toString()));
        if (params.containsKey("temperature"))
            insp.setTemperature(new java.math.BigDecimal(params.get("temperature").toString()));
        if (params.containsKey("noiseDb"))
            insp.setNoiseDb(new java.math.BigDecimal(params.get("noiseDb").toString()));
        if (params.containsKey("vibrationMm"))
            insp.setVibrationMm(new java.math.BigDecimal(params.get("vibrationMm").toString()));
        insp.setResult(safeStr(params.get("result"), "normal"));
        insp.setDescription(safeStr(params.get("description")));
        insp.setImageJson(safeStr(params.get("imageJson")));
        service.addInspection(insp);
        return Result.ok();
    }

    @PostMapping("/getSubstationInspection/{id}")
    public Result<SubstationInspection> getInspection(@PathVariable Long id) {
        SubstationInspection insp = service.getInspectionById(id);
        if (insp == null) return Result.fail("巡检记录不存在");
        return Result.ok(insp);
    }

    private Long safeLong(Object v) {
        if (v == null) return null;
        try { return Long.parseLong(v.toString()); } catch (NumberFormatException e) { return null; }
    }
    private String safeStr(Object v) {
        return v != null && !v.toString().isEmpty() ? v.toString() : null;
    }
    private String safeStr(Object v, String def) {
        String s = safeStr(v);
        return s != null ? s : def;
    }
}
