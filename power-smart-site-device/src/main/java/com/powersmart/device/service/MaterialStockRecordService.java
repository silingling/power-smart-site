package com.powersmart.device.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.annotation.OperateLog;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.common.util.PageHelper;
import com.powersmart.device.entity.MaterialInfo;
import com.powersmart.device.entity.MaterialStockRecord;
import com.powersmart.device.mapper.MaterialStockRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@OperateLog(module = "物料库存记录")
public class MaterialStockRecordService {

    private final MaterialStockRecordMapper materialStockRecordMapper;
    private final MaterialInfoService materialInfoService;

    public PageResult<MaterialStockRecord> list(Map<String, Object> params) {
        Page<MaterialStockRecord> page = PageHelper.of(params);
        LambdaQueryWrapper<MaterialStockRecord> wrapper = new LambdaQueryWrapper<>();
        if (params != null) {
            Object projectId = params.get("projectId");
            if (projectId != null) {
                wrapper.eq(MaterialStockRecord::getProjectId, Long.valueOf(projectId.toString()));
            }
            Object materialId = params.get("materialId");
            if (materialId != null) {
                wrapper.eq(MaterialStockRecord::getMaterialId, Long.valueOf(materialId.toString()));
            }
            Object recordType = params.get("recordType");
            if (recordType != null && StrUtil.isNotBlank(recordType.toString())) {
                wrapper.eq(MaterialStockRecord::getRecordType, recordType.toString());
            }
            Object startDate = params.get("startDate");
            Object endDate = params.get("endDate");
            if (startDate != null && StrUtil.isNotBlank(startDate.toString())) {
                wrapper.ge(MaterialStockRecord::getOperationTime, startDate.toString());
            }
            if (endDate != null && StrUtil.isNotBlank(endDate.toString())) {
                wrapper.le(MaterialStockRecord::getOperationTime, endDate.toString());
            }
        }
        wrapper.orderByDesc(MaterialStockRecord::getCreatedAt);
        return PageResult.from(materialStockRecordMapper.selectPage(page, wrapper));
    }

    public MaterialStockRecord getById(Long id) {
        return materialStockRecordMapper.selectById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    @OperateLog(action = "stockIn", description = "入库")
    public Result<Void> stockIn(MaterialStockRecord entity) {
        MaterialInfo material = materialInfoService.getById(entity.getMaterialId());
        if (material == null) {
            return Result.fail("物料不存在");
        }
        if (entity.getQuantity() == null || entity.getQuantity() <= 0) {
            return Result.fail("入库数量必须大于0");
        }
        if (entity.getOperationTime() == null) {
            entity.setOperationTime(LocalDateTime.now());
        }
        if (entity.getUnitPrice() == null) {
            entity.setUnitPrice(material.getUnitPrice() != null ? material.getUnitPrice() : BigDecimal.ZERO);
        }
        // Calculate totalAmount = quantity * unitPrice
        entity.setTotalAmount(entity.getUnitPrice().multiply(BigDecimal.valueOf(entity.getQuantity())));
        entity.setRecordType("in");
        entity.setRelatedBiz("purchase");
        materialStockRecordMapper.insert(entity);

        // Update stock
        materialInfoService.updateStock(entity.getMaterialId(), entity.getQuantity());
        return Result.ok();
    }

    @Transactional(rollbackFor = Exception.class)
    @OperateLog(action = "stockOut", description = "出库")
    public Result<Void> stockOut(MaterialStockRecord entity) {
        MaterialInfo material = materialInfoService.getById(entity.getMaterialId());
        if (material == null) {
            return Result.fail("物料不存在");
        }
        if (entity.getQuantity() == null || entity.getQuantity() <= 0) {
            return Result.fail("出库数量必须大于0");
        }
        // Check stock sufficiency
        if (material.getCurrentQuantity() < entity.getQuantity()) {
            return Result.fail("库存不足，当前库存：" + material.getCurrentQuantity() + "，请求出库：" + entity.getQuantity());
        }
        if (entity.getOperationTime() == null) {
            entity.setOperationTime(LocalDateTime.now());
        }
        if (entity.getUnitPrice() == null) {
            entity.setUnitPrice(material.getUnitPrice() != null ? material.getUnitPrice() : BigDecimal.ZERO);
        }
        entity.setTotalAmount(entity.getUnitPrice().multiply(BigDecimal.valueOf(entity.getQuantity())));
        entity.setRecordType("out");
        entity.setRelatedBiz("consumption");
        materialStockRecordMapper.insert(entity);

        // Update stock (negative delta)
        materialInfoService.updateStock(entity.getMaterialId(), -entity.getQuantity());
        return Result.ok();
    }

    @Transactional(rollbackFor = Exception.class)
    @OperateLog(action = "stockReturn", description = "退库")
    public Result<Void> stockReturn(MaterialStockRecord entity) {
        MaterialInfo material = materialInfoService.getById(entity.getMaterialId());
        if (material == null) {
            return Result.fail("物料不存在");
        }
        if (entity.getQuantity() == null || entity.getQuantity() <= 0) {
            return Result.fail("退库数量必须大于0");
        }
        if (entity.getOperationTime() == null) {
            entity.setOperationTime(LocalDateTime.now());
        }
        if (entity.getUnitPrice() == null) {
            entity.setUnitPrice(material.getUnitPrice() != null ? material.getUnitPrice() : BigDecimal.ZERO);
        }
        entity.setTotalAmount(entity.getUnitPrice().multiply(BigDecimal.valueOf(entity.getQuantity())));
        entity.setRecordType("return");
        entity.setRelatedBiz("return");
        materialStockRecordMapper.insert(entity);

        // Update stock (positive delta, like stockIn)
        materialInfoService.updateStock(entity.getMaterialId(), entity.getQuantity());
        return Result.ok();
    }

    public List<MaterialStockRecord> getByMaterial(Long materialId) {
        LambdaQueryWrapper<MaterialStockRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MaterialStockRecord::getMaterialId, materialId);
        wrapper.orderByDesc(MaterialStockRecord::getCreatedAt);
        return materialStockRecordMapper.selectList(wrapper);
    }
}
