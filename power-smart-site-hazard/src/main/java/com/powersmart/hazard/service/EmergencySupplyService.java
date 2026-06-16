package com.powersmart.hazard.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.annotation.OperateLog;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.util.PageHelper;
import com.powersmart.hazard.entity.EmergencySupply;
import com.powersmart.hazard.mapper.EmergencySupplyMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@OperateLog
public class EmergencySupplyService {

    private final EmergencySupplyMapper emergencySupplyMapper;

    public PageResult<EmergencySupply> list(Map<String, Object> params) {
        Page<EmergencySupply> page = PageHelper.of(params);
        LambdaQueryWrapper<EmergencySupply> wrapper = new LambdaQueryWrapper<>();
        if (params != null) {
            if (params.containsKey("projectId") && params.get("projectId") != null)
                wrapper.eq(EmergencySupply::getProjectId, Long.valueOf(params.get("projectId").toString()));
            if (params.containsKey("supplyType") && params.get("supplyType") != null)
                wrapper.eq(EmergencySupply::getSupplyType, params.get("supplyType").toString());
            if (params.containsKey("status") && params.get("status") != null)
                wrapper.eq(EmergencySupply::getStatus, params.get("status").toString());
            if (params.containsKey("minStockWarning") && Boolean.TRUE.toString().equals(params.get("minStockWarning").toString()))
                wrapper.apply("quantity <= min_quantity");
        }
        wrapper.orderByDesc(EmergencySupply::getCreatedAt);
        return PageResult.from(emergencySupplyMapper.selectPage(page, wrapper));
    }

    public EmergencySupply getById(Long id) {
        return emergencySupplyMapper.selectById(id);
    }

    public void add(EmergencySupply entity) {
        if (StrUtil.isBlank(entity.getSupplyName())) {
            throw new IllegalArgumentException("物资名称不能为空");
        }
        if (entity.getQuantity() == null) {
            entity.setQuantity(0);
        }
        if (StrUtil.isBlank(entity.getStatus())) {
            entity.setStatus("normal");
        }
        emergencySupplyMapper.insert(entity);
    }

    public void update(EmergencySupply entity) {
        EmergencySupply existing = emergencySupplyMapper.selectById(entity.getId());
        if (existing == null) {
            throw new IllegalArgumentException("应急物资不存在");
        }
        if (StrUtil.isNotBlank(entity.getSupplyName())) {
            existing.setSupplyName(entity.getSupplyName());
        }
        if (StrUtil.isNotBlank(entity.getSupplyType())) {
            existing.setSupplyType(entity.getSupplyType());
        }
        if (entity.getSpecification() != null) {
            existing.setSpecification(entity.getSpecification());
        }
        if (entity.getUnit() != null) {
            existing.setUnit(entity.getUnit());
        }
        if (entity.getQuantity() != null) {
            existing.setQuantity(entity.getQuantity());
        }
        if (entity.getMinQuantity() != null) {
            existing.setMinQuantity(entity.getMinQuantity());
        }
        if (entity.getLocation() != null) {
            existing.setLocation(entity.getLocation());
        }
        if (entity.getStorageCondition() != null) {
            existing.setStorageCondition(entity.getStorageCondition());
        }
        if (entity.getExpiryDate() != null) {
            existing.setExpiryDate(entity.getExpiryDate());
        }
        if (entity.getSupplier() != null) {
            existing.setSupplier(entity.getSupplier());
        }
        if (entity.getContactPhone() != null) {
            existing.setContactPhone(entity.getContactPhone());
        }
        if (entity.getRemark() != null) {
            existing.setRemark(entity.getRemark());
        }
        if (StrUtil.isNotBlank(entity.getStatus())) {
            existing.setStatus(entity.getStatus());
        }
        // Recalculate status based on stock level
        recalculateStatus(existing);
        emergencySupplyMapper.updateById(existing);
    }

    public void delete(Long id) {
        emergencySupplyMapper.deleteById(id);
    }

    public List<EmergencySupply> getLowStockList(Long projectId) {
        return emergencySupplyMapper.selectList(
                new LambdaQueryWrapper<EmergencySupply>()
                        .eq(EmergencySupply::getProjectId, projectId)
                        .apply("quantity <= min_quantity")
                        .orderByAsc(EmergencySupply::getQuantity));
    }

    public Map<String, Object> getInventoryStats(Long projectId) {
        Map<String, Object> stats = new LinkedHashMap<>();
        List<EmergencySupply> supplies = emergencySupplyMapper.selectList(
                new LambdaQueryWrapper<EmergencySupply>()
                        .eq(EmergencySupply::getProjectId, projectId));

        // Summary by supply type
        Map<String, Long> typeCount = supplies.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getSupplyType() != null ? s.getSupplyType() : "其他",
                        Collectors.counting()));
        stats.put("typeCount", typeCount);

        // Total items and low stock count
        stats.put("totalCount", supplies.size());
        long lowStockCount = supplies.stream()
                .filter(s -> s.getQuantity() != null && s.getMinQuantity() != null
                        && s.getQuantity() <= s.getMinQuantity())
                .count();
        stats.put("lowStockCount", lowStockCount);

        // Total quantity by type
        Map<String, Integer> typeQuantity = supplies.stream()
                .filter(s -> s.getSupplyType() != null && s.getQuantity() != null)
                .collect(Collectors.groupingBy(
                        EmergencySupply::getSupplyType,
                        Collectors.summingInt(EmergencySupply::getQuantity)));
        stats.put("typeQuantity", typeQuantity);

        stats.put("projectId", projectId);
        return stats;
    }

    private void recalculateStatus(EmergencySupply supply) {
        if (supply.getQuantity() != null && supply.getMinQuantity() != null) {
            if (supply.getQuantity() <= 0) {
                supply.setStatus("out_of_stock");
            } else if (supply.getQuantity() <= supply.getMinQuantity()) {
                supply.setStatus("low_stock");
            } else {
                supply.setStatus("normal");
            }
        }
    }
}
