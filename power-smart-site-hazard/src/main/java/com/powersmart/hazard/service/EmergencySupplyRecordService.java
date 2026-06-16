package com.powersmart.hazard.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.annotation.OperateLog;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.util.PageHelper;
import com.powersmart.hazard.entity.EmergencySupply;
import com.powersmart.hazard.entity.EmergencySupplyRecord;
import com.powersmart.hazard.mapper.EmergencySupplyMapper;
import com.powersmart.hazard.mapper.EmergencySupplyRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@OperateLog
public class EmergencySupplyRecordService {

    private final EmergencySupplyRecordMapper emergencySupplyRecordMapper;
    private final EmergencySupplyMapper emergencySupplyMapper;

    public PageResult<EmergencySupplyRecord> list(Map<String, Object> params) {
        Page<EmergencySupplyRecord> page = PageHelper.of(params);
        LambdaQueryWrapper<EmergencySupplyRecord> wrapper = new LambdaQueryWrapper<>();
        if (params != null) {
            if (params.containsKey("projectId") && params.get("projectId") != null)
                wrapper.eq(EmergencySupplyRecord::getProjectId, Long.valueOf(params.get("projectId").toString()));
            if (params.containsKey("supplyId") && params.get("supplyId") != null)
                wrapper.eq(EmergencySupplyRecord::getSupplyId, Long.valueOf(params.get("supplyId").toString()));
            if (params.containsKey("recordType") && params.get("recordType") != null)
                wrapper.eq(EmergencySupplyRecord::getRecordType, params.get("recordType").toString());
        }
        wrapper.orderByDesc(EmergencySupplyRecord::getOperationTime);
        return PageResult.from(emergencySupplyRecordMapper.selectPage(page, wrapper));
    }

    @Transactional(rollbackFor = Exception.class)
    public void add(EmergencySupplyRecord entity) {
        if (entity.getSupplyId() == null) {
            throw new IllegalArgumentException("物资ID不能为空");
        }
        if (StrUtil.isBlank(entity.getRecordType())) {
            throw new IllegalArgumentException("出入库类型不能为空");
        }
        if (entity.getQuantity() == null || entity.getQuantity() <= 0) {
            throw new IllegalArgumentException("出入库数量必须大于0");
        }

        if (entity.getOperationTime() == null) {
            entity.setOperationTime(LocalDateTime.now());
        }

        // Update supply stock
        EmergencySupply supply = emergencySupplyMapper.selectById(entity.getSupplyId());
        if (supply == null) {
            throw new IllegalArgumentException("物资不存在");
        }

        int delta;
        if ("in".equals(entity.getRecordType()) || "入库".equals(entity.getRecordType())) {
            delta = entity.getQuantity();
        } else if ("out".equals(entity.getRecordType()) || "出库".equals(entity.getRecordType())) {
            delta = -entity.getQuantity();
            if (supply.getQuantity() != null && supply.getQuantity() + delta < 0) {
                throw new IllegalArgumentException("库存不足，当前库存：" + supply.getQuantity());
            }
        } else {
            throw new IllegalArgumentException("不支持的出入库类型：" + entity.getRecordType());
        }

        supply.setQuantity(supply.getQuantity() != null ? supply.getQuantity() + delta : delta);
        // Recalculate status
        if (supply.getQuantity() <= 0) {
            supply.setStatus("out_of_stock");
        } else if (supply.getMinQuantity() != null && supply.getQuantity() <= supply.getMinQuantity()) {
            supply.setStatus("low_stock");
        } else {
            supply.setStatus("normal");
        }
        emergencySupplyMapper.updateById(supply);

        emergencySupplyRecordMapper.insert(entity);
    }

    public List<EmergencySupplyRecord> getBySupply(Long supplyId) {
        return emergencySupplyRecordMapper.selectList(
                new LambdaQueryWrapper<EmergencySupplyRecord>()
                        .eq(EmergencySupplyRecord::getSupplyId, supplyId)
                        .orderByDesc(EmergencySupplyRecord::getOperationTime));
    }
}
