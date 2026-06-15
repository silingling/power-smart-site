package com.powersmart.device.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.device.entity.SubstationEquipment;
import com.powersmart.device.entity.SubstationInspection;
import com.powersmart.device.mapper.SubstationEquipmentMapper;
import com.powersmart.device.service.SubstationEquipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 变电站设备台账 — /build/substationEquipment/*  /build/substationInspection/*
 *
 * 符合萤丰前端 build 命名规范
 */
@RestController
@RequiredArgsConstructor
public class SubstationEquipmentController {

    private final SubstationEquipmentService service;

    // ==================== 设备台账 CRUD ====================

    /** 分页查询设备列表 */
    @PostMapping("/build/substationEquipment/querySubstationEquipmentList")
    public Result<PageResult<Map<String, Object>>> queryList(@RequestBody(required = false) Map<String, Object> params) {
        Page<SubstationEquipment> page = service.queryPage(params);
        List<Map<String, Object>> list = page.getRecords().stream()
                .map(this::toMap).collect(Collectors.toList());
        return Result.ok(PageResult.of(list, page.getTotal(), (int) page.getCurrent(), (int) page.getSize()));
    }

    /** 获取设备详情 */
    @PostMapping("/build/substationEquipment/getSubstationEquipment/{id}")
    public Result<Map<String, Object>> get(@PathVariable Long id) {
        SubstationEquipment eq = service.getById(id);
        if (eq == null) return Result.fail("设备不存在");
        return Result.ok(toMap(eq));
    }

    /** 新增设备 */
    @PostMapping("/build/substationEquipment/addSubstationEquipment")
    public Result<Void> add(@RequestBody Map<String, Object> params) {
        SubstationEquipment eq = buildFromParams(null, params);
        service.add(eq);
        return Result.ok();
    }

    /** 更新设备 */
    @PostMapping("/build/substationEquipment/setSubstationEquipment")
    public Result<Void> set(@RequestBody Map<String, Object> params) {
        Long id = safeLong(params.get("id"));
        if (id == null) return Result.fail("id 不能为空");
        SubstationEquipment eq = service.getById(id);
        if (eq == null) return Result.fail("设备不存在");
        service.update(buildFromParams(eq, params));
        return Result.ok();
    }

    /** 删除设备 */
    @PostMapping("/build/substationEquipment/delSubstationEquipment/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return Result.ok();
    }

    /** 获取某项目下设备类型列表 */
    @PostMapping("/build/substationEquipment/getSubstationDeviceTypeList")
    public Result<List<String>> getDeviceTypes(@RequestBody Map<String, Object> params) {
        Long projectId = safeLong(params.get("projectId"));
        if (projectId == null) return Result.fail("projectId 不能为空");
        return Result.ok(service.getDeviceTypes(projectId));
    }

    // ==================== 巡检记录 ====================

    /** 分页查询巡检记录 */
    @PostMapping("/build/substationInspection/querySubstationInspectionList")
    public Result<PageResult<Map<String, Object>>> queryInspectionList(@RequestBody(required = false) Map<String, Object> params) {
        Page<SubstationInspection> page = service.queryInspectionPage(params);
        List<Map<String, Object>> list = page.getRecords().stream().map(insp -> {
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

    /** 新增巡检记录 */
    @PostMapping("/build/substationInspection/addSubstationInspection")
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

    /** 获取巡检详情 */
    @PostMapping("/build/substationInspection/getSubstationInspection/{id}")
    public Result<SubstationInspection> getInspection(@PathVariable Long id) {
        SubstationInspection insp = service.getInspectionById(id);
        if (insp == null) return Result.fail("巡检记录不存在");
        return Result.ok(insp);
    }

    // ==================== 帮助方法 ====================

    private Map<String, Object> toMap(SubstationEquipment e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", e.getId());
        m.put("projectId", e.getProjectId());
        m.put("deviceType", e.getDeviceType());
        m.put("deviceCode", e.getDeviceCode());
        m.put("deviceName", e.getDeviceName());
        m.put("bayNumber", e.getBayNumber());
        m.put("voltageLevel", e.getVoltageLevel());
        // 通用资产
        m.put("manufacturer", e.getManufacturer());
        m.put("model", e.getModel());
        m.put("serialNumber", e.getSerialNumber());
        m.put("manufactureDate", e.getManufactureDate() != null ? e.getManufactureDate().toString() : "");
        m.put("installDate", e.getInstallDate() != null ? e.getInstallDate().toString() : "");
        m.put("commissionDate", e.getCommissionDate() != null ? e.getCommissionDate().toString() : "");
        m.put("designLifeYears", e.getDesignLifeYears());
        m.put("status", e.getStatus());
        m.put("lastMaintenanceDate", e.getLastMaintenanceDate() != null ? e.getLastMaintenanceDate().toString() : "");
        m.put("nextMaintenanceDate", e.getNextMaintenanceDate() != null ? e.getNextMaintenanceDate().toString() : "");
        // GIS
        m.put("gasType", e.getGasType());
        m.put("sf6PressureKpa", e.getSf6PressureKpa());
        m.put("sf6AlarmPressureKpa", e.getSf6AlarmPressureKpa());
        m.put("sealedPartsCount", e.getSealedPartsCount());
        // Transformer
        m.put("ratedCapacityMva", e.getRatedCapacityMva());
        m.put("coolingMethod", e.getCoolingMethod());
        m.put("tapChangerType", e.getTapChangerType());
        m.put("tapChangerPositions", e.getTapChangerPositions());
        m.put("oilType", e.getOilType());
        m.put("oilWeightKg", e.getOilWeightKg());
        m.put("windingConnection", e.getWindingConnection());
        // Breaker
        m.put("ratedCurrentKa", e.getRatedCurrentKa());
        m.put("ratedVoltageKv", e.getRatedVoltageKv());
        m.put("ratedBreakingCurrentKa", e.getRatedBreakingCurrentKa());
        m.put("operatingMechanism", e.getOperatingMechanism());
        m.put("operatingVoltageV", e.getOperatingVoltageV());
        m.put("mechanicalOperations", e.getMechanicalOperations());
        m.put("breakingCount", e.getBreakingCount());
        // 位置/附件
        m.put("locationDesc", e.getLocationDesc());
        m.put("longitude", e.getLongitude());
        m.put("latitude", e.getLatitude());
        m.put("parentId", e.getParentId());
        m.put("videoMonitorId", e.getVideoMonitorId());
        m.put("attachmentJson", e.getAttachmentJson());
        m.put("remark", e.getRemark());
        m.put("createBy", e.getCreateBy());
        m.put("createdAt", e.getCreatedAt() != null ? e.getCreatedAt().toString() : "");
        m.put("updatedAt", e.getUpdatedAt() != null ? e.getUpdatedAt().toString() : "");
        return m;
    }

    private SubstationEquipment buildFromParams(SubstationEquipment e, Map<String, Object> p) {
        if (e == null) {
            e = new SubstationEquipment();
            e.setStatus("in_service");
        }
        if (p.containsKey("projectId")) e.setProjectId(safeLong(p.get("projectId")));
        if (p.containsKey("deviceType")) e.setDeviceType(p.get("deviceType").toString());
        if (p.containsKey("deviceCode")) e.setDeviceCode(p.get("deviceCode").toString());
        if (p.containsKey("deviceName")) e.setDeviceName(p.get("deviceName").toString());
        if (p.containsKey("bayNumber")) e.setBayNumber(p.get("bayNumber").toString());
        if (p.containsKey("voltageLevel")) e.setVoltageLevel(p.get("voltageLevel").toString());
        if (p.containsKey("manufacturer")) e.setManufacturer(p.get("manufacturer").toString());
        if (p.containsKey("model")) e.setModel(p.get("model").toString());
        if (p.containsKey("serialNumber")) e.setSerialNumber(p.get("serialNumber").toString());
        if (p.containsKey("status")) e.setStatus(p.get("status").toString());
        // 日期
        if (p.containsKey("manufactureDate"))
            e.setManufactureDate(java.time.LocalDate.parse(p.get("manufactureDate").toString()));
        if (p.containsKey("installDate"))
            e.setInstallDate(java.time.LocalDate.parse(p.get("installDate").toString()));
        if (p.containsKey("commissionDate"))
            e.setCommissionDate(java.time.LocalDate.parse(p.get("commissionDate").toString()));
        if (p.containsKey("lastMaintenanceDate"))
            e.setLastMaintenanceDate(java.time.LocalDate.parse(p.get("lastMaintenanceDate").toString()));
        if (p.containsKey("nextMaintenanceDate"))
            e.setNextMaintenanceDate(java.time.LocalDate.parse(p.get("nextMaintenanceDate").toString()));
        // 数值字段
        if (p.containsKey("designLifeYears"))
            e.setDesignLifeYears(Integer.parseInt(p.get("designLifeYears").toString()));
        if (p.containsKey("sf6PressureKpa"))
            e.setSf6PressureKpa(new java.math.BigDecimal(p.get("sf6PressureKpa").toString()));
        if (p.containsKey("sf6AlarmPressureKpa"))
            e.setSf6AlarmPressureKpa(new java.math.BigDecimal(p.get("sf6AlarmPressureKpa").toString()));
        if (p.containsKey("sealedPartsCount"))
            e.setSealedPartsCount(Integer.parseInt(p.get("sealedPartsCount").toString()));
        if (p.containsKey("ratedCapacityMva"))
            e.setRatedCapacityMva(new java.math.BigDecimal(p.get("ratedCapacityMva").toString()));
        if (p.containsKey("ratedCurrentKa"))
            e.setRatedCurrentKa(new java.math.BigDecimal(p.get("ratedCurrentKa").toString()));
        if (p.containsKey("ratedVoltageKv"))
            e.setRatedVoltageKv(new java.math.BigDecimal(p.get("ratedVoltageKv").toString()));
        if (p.containsKey("ratedBreakingCurrentKa"))
            e.setRatedBreakingCurrentKa(new java.math.BigDecimal(p.get("ratedBreakingCurrentKa").toString()));
        if (p.containsKey("mechanicalOperations"))
            e.setMechanicalOperations(Integer.parseInt(p.get("mechanicalOperations").toString()));
        if (p.containsKey("breakingCount"))
            e.setBreakingCount(Integer.parseInt(p.get("breakingCount").toString()));
        if (p.containsKey("operatingVoltageV"))
            e.setOperatingVoltageV(Integer.parseInt(p.get("operatingVoltageV").toString()));
        // 字符串
        if (p.containsKey("gasType")) e.setGasType(p.get("gasType").toString());
        if (p.containsKey("coolingMethod")) e.setCoolingMethod(p.get("coolingMethod").toString());
        if (p.containsKey("tapChangerType")) e.setTapChangerType(p.get("tapChangerType").toString());
        if (p.containsKey("oilType")) e.setOilType(p.get("oilType").toString());
        if (p.containsKey("windingConnection")) e.setWindingConnection(p.get("windingConnection").toString());
        if (p.containsKey("operatingMechanism")) e.setOperatingMechanism(p.get("operatingMechanism").toString());
        if (p.containsKey("locationDesc")) e.setLocationDesc(p.get("locationDesc").toString());
        if (p.containsKey("remark")) e.setRemark(p.get("remark").toString());
        if (p.containsKey("createBy")) e.setCreateBy(p.get("createBy").toString());
        if (p.containsKey("attachmentJson")) e.setAttachmentJson(p.get("attachmentJson").toString());
        if (p.containsKey("videoMonitorId")) e.setVideoMonitorId(safeLong(p.get("videoMonitorId")));
        if (p.containsKey("parentId")) e.setParentId(safeLong(p.get("parentId")));

        return e;
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
