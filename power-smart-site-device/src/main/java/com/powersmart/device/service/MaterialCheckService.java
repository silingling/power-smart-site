package com.powersmart.device.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.annotation.OperateLog;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.common.util.PageHelper;
import com.powersmart.device.entity.MaterialCheck;
import com.powersmart.device.entity.MaterialCheckItem;
import com.powersmart.device.mapper.MaterialCheckItemMapper;
import com.powersmart.device.mapper.MaterialCheckMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@OperateLog(module = "物料盘点")
public class MaterialCheckService {

    private final MaterialCheckMapper materialCheckMapper;
    private final MaterialCheckItemMapper materialCheckItemMapper;
    private final MaterialInfoService materialInfoService;

    public PageResult<MaterialCheck> list(Map<String, Object> params) {
        Page<MaterialCheck> page = PageHelper.of(params);
        LambdaQueryWrapper<MaterialCheck> wrapper = new LambdaQueryWrapper<>();
        if (params != null) {
            Object projectId = params.get("projectId");
            if (projectId != null) {
                wrapper.eq(MaterialCheck::getProjectId, Long.valueOf(projectId.toString()));
            }
            Object status = params.get("status");
            if (status != null && StrUtil.isNotBlank(status.toString())) {
                wrapper.eq(MaterialCheck::getStatus, status.toString());
            }
            Object startDate = params.get("startDate");
            Object endDate = params.get("endDate");
            if (startDate != null && StrUtil.isNotBlank(startDate.toString())) {
                wrapper.ge(MaterialCheck::getCheckDate, startDate.toString());
            }
            if (endDate != null && StrUtil.isNotBlank(endDate.toString())) {
                wrapper.le(MaterialCheck::getCheckDate, endDate.toString());
            }
        }
        wrapper.orderByDesc(MaterialCheck::getCreatedAt);
        return PageResult.from(materialCheckMapper.selectPage(page, wrapper));
    }

    public MaterialCheck getById(Long id) {
        return materialCheckMapper.selectById(id);
    }

    @OperateLog(action = "add", description = "新增盘点")
    public Result<Void> add(MaterialCheck entity) {
        if (entity.getStatus() == null) {
            entity.setStatus("draft");
        }
        materialCheckMapper.insert(entity);
        return Result.ok();
    }

    @OperateLog(action = "update", description = "更新盘点")
    public Result<Void> update(MaterialCheck entity) {
        if (entity.getId() == null) {
            return Result.fail("ID不能为空");
        }
        materialCheckMapper.updateById(entity);
        return Result.ok();
    }

    @OperateLog(action = "delete", description = "删除盘点")
    public Result<Void> delete(Long id) {
        materialCheckMapper.deleteById(id);
        return Result.ok();
    }

    @Transactional(rollbackFor = Exception.class)
    @OperateLog(action = "submitCheckItems", description = "提交盘点明细")
    public Result<Void> submitCheckItems(Long checkId, List<MaterialCheckItem> items) {
        MaterialCheck check = materialCheckMapper.selectById(checkId);
        if (check == null) {
            return Result.fail("盘点单不存在");
        }
        // Delete existing items
        LambdaQueryWrapper<MaterialCheckItem> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(MaterialCheckItem::getCheckId, checkId);
        materialCheckItemMapper.delete(deleteWrapper);

        // Insert new items
        for (MaterialCheckItem item : items) {
            item.setCheckId(checkId);
            if (item.getBookQuantity() == null) {
                item.setBookQuantity(BigDecimal.ZERO);
            }
            if (item.getActualQuantity() == null) {
                item.setActualQuantity(BigDecimal.ZERO);
            }
            // Calculate difference = actual - book
            item.setDifference(item.getActualQuantity().subtract(item.getBookQuantity()));
            if (item.getUnitPrice() == null) {
                item.setUnitPrice(BigDecimal.ZERO);
            }
            // Calculate differenceAmount = difference * unitPrice
            item.setDifferenceAmount(item.getDifference().multiply(item.getUnitPrice()));
            materialCheckItemMapper.insert(item);
        }
        return Result.ok();
    }

    @Transactional(rollbackFor = Exception.class)
    @OperateLog(action = "approveCheck", description = "审批盘点")
    public Result<Void> approveCheck(Long checkId) {
        MaterialCheck check = materialCheckMapper.selectById(checkId);
        if (check == null) {
            return Result.fail("盘点单不存在");
        }
        if (!"submitted".equals(check.getStatus())) {
            return Result.fail("只有已提交的盘点单才能审批，当前状态：" + check.getStatus());
        }

        // Get all check items
        LambdaQueryWrapper<MaterialCheckItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(MaterialCheckItem::getCheckId, checkId);
        List<MaterialCheckItem> items = materialCheckItemMapper.selectList(itemWrapper);

        for (MaterialCheckItem item : items) {
            if (item.getDifference() != null && item.getDifference().compareTo(BigDecimal.ZERO) != 0) {
                // Adjust stock by difference
                materialInfoService.updateStock(item.getMaterialId(), item.getDifference().intValue());
            }
        }

        // Update check status
        check.setStatus("approved");
        materialCheckMapper.updateById(check);
        return Result.ok();
    }

    @OperateLog(action = "submit", description = "提交盘点")
    public Result<Void> submit(Long checkId) {
        MaterialCheck check = materialCheckMapper.selectById(checkId);
        if (check == null) {
            return Result.fail("盘点单不存在");
        }
        if (!"draft".equals(check.getStatus())) {
            return Result.fail("只有草稿状态的盘点单才能提交，当前状态：" + check.getStatus());
        }
        check.setStatus("submitted");
        materialCheckMapper.updateById(check);
        return Result.ok();
    }

    public List<MaterialCheckItem> getCheckItems(Long checkId) {
        LambdaQueryWrapper<MaterialCheckItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MaterialCheckItem::getCheckId, checkId);
        wrapper.orderByAsc(MaterialCheckItem::getCreatedAt);
        return materialCheckItemMapper.selectList(wrapper);
    }
}
