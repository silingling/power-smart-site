package com.powersmart.device.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.annotation.OperateLog;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.common.util.PageHelper;
import com.powersmart.device.entity.MaterialInfo;
import com.powersmart.device.mapper.MaterialInfoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
@OperateLog(module = "物料信息")
public class MaterialInfoService {

    private final MaterialInfoMapper materialInfoMapper;

    public PageResult<MaterialInfo> list(Map<String, Object> params) {
        Page<MaterialInfo> page = PageHelper.of(params);
        LambdaQueryWrapper<MaterialInfo> wrapper = new LambdaQueryWrapper<>();
        if (params != null) {
            Object projectId = params.get("projectId");
            if (projectId != null) {
                wrapper.eq(MaterialInfo::getProjectId, Long.valueOf(projectId.toString()));
            }
            Object categoryId = params.get("categoryId");
            if (categoryId != null) {
                wrapper.eq(MaterialInfo::getCategoryId, Long.valueOf(categoryId.toString()));
            }
            Object status = params.get("status");
            if (status != null && StrUtil.isNotBlank(status.toString())) {
                wrapper.eq(MaterialInfo::getStatus, status.toString());
            }
            Object keyword = params.get("keyword");
            if (keyword != null && StrUtil.isNotBlank(keyword.toString())) {
                String kw = keyword.toString();
                wrapper.and(w -> w.like(MaterialInfo::getMaterialName, kw)
                        .or().like(MaterialInfo::getMaterialCode, kw));
            }
            Object lowStock = params.get("lowStock");
            if (lowStock != null && Boolean.parseBoolean(lowStock.toString())) {
                wrapper.apply("current_quantity <= min_quantity");
            }
        }
        wrapper.orderByDesc(MaterialInfo::getCreatedAt);
        return PageResult.from(materialInfoMapper.selectPage(page, wrapper));
    }

    public MaterialInfo getById(Long id) {
        return materialInfoMapper.selectById(id);
    }

    @OperateLog(action = "add", description = "新增物料")
    public Result<Void> add(MaterialInfo entity) {
        if (StrUtil.isBlank(entity.getMaterialName())) {
            return Result.fail("物料名称不能为空");
        }
        if (StrUtil.isBlank(entity.getMaterialCode())) {
            entity.setMaterialCode(generateMaterialCode());
        }
        if (StrUtil.isBlank(entity.getStatus())) {
            entity.setStatus("normal");
        }
        if (entity.getCurrentQuantity() == null) {
            entity.setCurrentQuantity(0);
        }
        if (entity.getMinQuantity() == null) {
            entity.setMinQuantity(0);
        }
        materialInfoMapper.insert(entity);
        return Result.ok();
    }

    @OperateLog(action = "update", description = "更新物料")
    public Result<Void> update(MaterialInfo entity) {
        if (entity.getId() == null) {
            return Result.fail("ID不能为空");
        }
        MaterialInfo existing = materialInfoMapper.selectById(entity.getId());
        if (existing == null) {
            return Result.fail("物料不存在");
        }
        materialInfoMapper.updateById(entity);
        // Auto-recalculate status
        MaterialInfo updated = materialInfoMapper.selectById(entity.getId());
        recalculateStatus(updated);
        return Result.ok();
    }

    @OperateLog(action = "delete", description = "删除物料")
    public Result<Void> delete(Long id) {
        materialInfoMapper.deleteById(id);
        return Result.ok();
    }

    @OperateLog(action = "updateStock", description = "调整库存")
    public Result<Void> updateStock(Long id, Integer delta) {
        MaterialInfo material = materialInfoMapper.selectById(id);
        if (material == null) {
            return Result.fail("物料不存在");
        }
        int newQuantity = material.getCurrentQuantity() + delta;
        if (newQuantity < 0) {
            return Result.fail("库存不足，当前库存为：" + material.getCurrentQuantity());
        }
        material.setCurrentQuantity(newQuantity);
        recalculateStatus(material);
        materialInfoMapper.updateById(material);
        return Result.ok();
    }

    public List<MaterialInfo> getLowStockList(Long projectId) {
        LambdaQueryWrapper<MaterialInfo> wrapper = new LambdaQueryWrapper<>();
        if (projectId != null) {
            wrapper.eq(MaterialInfo::getProjectId, projectId);
        }
        wrapper.apply("current_quantity <= min_quantity");
        wrapper.orderByAsc(MaterialInfo::getCurrentQuantity);
        return materialInfoMapper.selectList(wrapper);
    }

    public Map<String, Object> getInventoryStats(Long projectId) {
        Map<String, Object> stats = new HashMap<>();

        LambdaQueryWrapper<MaterialInfo> wrapper = new LambdaQueryWrapper<>();
        if (projectId != null) {
            wrapper.eq(MaterialInfo::getProjectId, projectId);
        }

        List<MaterialInfo> allMaterials = materialInfoMapper.selectList(wrapper);

        long totalCount = allMaterials.size();
        long lowStockCount = allMaterials.stream().filter(m -> {
            int min = m.getMinQuantity() != null ? m.getMinQuantity() : 0;
            int cur = m.getCurrentQuantity() != null ? m.getCurrentQuantity() : 0;
            return cur <= min;
        }).count();
        long outOfStockCount = allMaterials.stream().filter(m ->
                "out_of_stock".equals(m.getStatus())).count();

        BigDecimal totalValue = allMaterials.stream()
                .filter(m -> m.getUnitPrice() != null && m.getCurrentQuantity() != null)
                .map(m -> m.getUnitPrice().multiply(BigDecimal.valueOf(m.getCurrentQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<Long, Long> categoryCount = new HashMap<>();
        for (MaterialInfo m : allMaterials) {
            Long cid = m.getCategoryId();
            categoryCount.merge(cid, 1L, Long::sum);
        }

        stats.put("totalCount", totalCount);
        stats.put("lowStockCount", lowStockCount);
        stats.put("outOfStockCount", outOfStockCount);
        stats.put("totalValue", totalValue);
        stats.put("categoryCount", categoryCount);

        return stats;
    }

    // --- private helpers ---

    private String generateMaterialCode() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int seq = new Random().nextInt(10000);
        return "MAT-" + datePart + "-" + String.format("%04d", seq);
    }

    private void recalculateStatus(MaterialInfo material) {
        String currentStatus = material.getStatus();
        if ("discontinued".equals(currentStatus)) {
            return;
        }
        Integer quantity = material.getCurrentQuantity();
        if (quantity == null || quantity <= 0) {
            material.setStatus("out_of_stock");
        } else {
            material.setStatus("normal");
        }
    }
}
