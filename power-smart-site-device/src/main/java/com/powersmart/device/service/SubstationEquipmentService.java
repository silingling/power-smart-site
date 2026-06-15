package com.powersmart.device.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.device.entity.SubstationEquipment;
import com.powersmart.device.entity.SubstationInspection;
import com.powersmart.device.mapper.SubstationEquipmentMapper;
import com.powersmart.device.mapper.SubstationInspectionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 变电站设备台账服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubstationEquipmentService {

    private final SubstationEquipmentMapper equipmentMapper;
    private final SubstationInspectionMapper inspectionMapper;

    // ===================== 设备台账 CRUD =====================

    public Page<SubstationEquipment> queryPage(Map<String, Object> params) {
        int pageNum = 1, pageSize = 20;
        if (params != null) {
            if (params.containsKey("page")) pageNum = Integer.parseInt(params.get("page").toString());
            if (params.containsKey("pageSize") || params.containsKey("limit"))
                pageSize = Integer.parseInt(params.getOrDefault("pageSize", params.getOrDefault("limit", "20")).toString());
        }
        Page<SubstationEquipment> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<SubstationEquipment> wrapper = new LambdaQueryWrapper<>();
        if (params != null) {
            if (params.containsKey("projectId"))
                wrapper.eq(SubstationEquipment::getProjectId, Long.valueOf(params.get("projectId").toString()));
            if (params.containsKey("deviceType"))
                wrapper.eq(SubstationEquipment::getDeviceType, params.get("deviceType").toString());
            if (params.containsKey("status"))
                wrapper.eq(SubstationEquipment::getStatus, params.get("status").toString());
            if (params.containsKey("keyword")) {
                String kw = "%" + params.get("keyword") + "%";
                wrapper.and(w -> w.like(SubstationEquipment::getDeviceName, kw)
                        .or().like(SubstationEquipment::getDeviceCode, kw)
                        .or().like(SubstationEquipment::getModel, kw));
            }
        }
        wrapper.orderByDesc(SubstationEquipment::getCreatedAt);
        return equipmentMapper.selectPage(page, wrapper);
    }

    public SubstationEquipment getById(Long id) {
        return equipmentMapper.selectById(id);
    }

    public void add(SubstationEquipment entity) {
        equipmentMapper.insert(entity);
    }

    public void update(SubstationEquipment entity) {
        equipmentMapper.updateById(entity);
    }

    public void delete(Long id) {
        equipmentMapper.deleteById(id);
    }

    public List<String> getDeviceTypes(Long projectId) {
        return equipmentMapper.selectDistinctDeviceTypes(projectId);
    }

    // ===================== 巡检记录 =====================

    public Page<SubstationInspection> queryInspectionPage(Map<String, Object> params) {
        int pageNum = 1, pageSize = 20;
        if (params != null) {
            if (params.containsKey("page")) pageNum = Integer.parseInt(params.get("page").toString());
            if (params.containsKey("pageSize")) pageSize = Integer.parseInt(params.get("pageSize").toString());
        }
        Page<SubstationInspection> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SubstationInspection> wrapper = new LambdaQueryWrapper<SubstationInspection>()
                .eq(params != null && params.containsKey("equipmentId"),
                        SubstationInspection::getEquipmentId,
                        params != null ? params.get("equipmentId") : null)
                .orderByDesc(SubstationInspection::getInspectionDate);
        return inspectionMapper.selectPage(page, wrapper);
    }

    public void addInspection(SubstationInspection inspection) {
        inspectionMapper.insert(inspection);
    }

    public SubstationInspection getInspectionById(Long id) {
        return inspectionMapper.selectById(id);
    }
}
