package com.powersmart.device.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.annotation.OperateLog;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.common.util.PageHelper;
import com.powersmart.device.entity.MaterialCategory;
import com.powersmart.device.mapper.MaterialCategoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@OperateLog(module = "物料分类")
public class MaterialCategoryService {

    private final MaterialCategoryMapper materialCategoryMapper;

    public PageResult<MaterialCategory> list(Map<String, Object> params) {
        Page<MaterialCategory> page = PageHelper.of(params);
        LambdaQueryWrapper<MaterialCategory> wrapper = new LambdaQueryWrapper<>();
        if (params != null) {
            Object projectId = params.get("projectId");
            if (projectId != null) {
                wrapper.eq(MaterialCategory::getProjectId, Long.valueOf(projectId.toString()));
            }
            Object categoryName = params.get("categoryName");
            if (categoryName != null && StrUtil.isNotBlank(categoryName.toString())) {
                wrapper.like(MaterialCategory::getCategoryName, categoryName.toString());
            }
        }
        wrapper.orderByAsc(MaterialCategory::getParentId).orderByAsc(MaterialCategory::getSortOrder);
        return PageResult.from(materialCategoryMapper.selectPage(page, wrapper));
    }

    public MaterialCategory getById(Long id) {
        return materialCategoryMapper.selectById(id);
    }

    @OperateLog(action = "add", description = "新增物料分类")
    public Result<Void> add(MaterialCategory entity) {
        if (StrUtil.isBlank(entity.getCategoryName())) {
            return Result.fail("分类名称不能为空");
        }
        if (entity.getParentId() == null) {
            entity.setParentId(0L);
        }
        materialCategoryMapper.insert(entity);
        return Result.ok();
    }

    @OperateLog(action = "update", description = "更新物料分类")
    public Result<Void> update(MaterialCategory entity) {
        if (entity.getId() == null) {
            return Result.fail("ID不能为空");
        }
        materialCategoryMapper.updateById(entity);
        return Result.ok();
    }

    @OperateLog(action = "delete", description = "删除物料分类")
    public Result<Void> delete(Long id) {
        materialCategoryMapper.deleteById(id);
        return Result.ok();
    }

    public List<MaterialCategory> getTree(Long projectId) {
        LambdaQueryWrapper<MaterialCategory> wrapper = new LambdaQueryWrapper<>();
        if (projectId != null) {
            wrapper.eq(MaterialCategory::getProjectId, projectId);
        }
        wrapper.orderByAsc(MaterialCategory::getParentId).orderByAsc(MaterialCategory::getSortOrder);
        return materialCategoryMapper.selectList(wrapper);
    }

    public List<MaterialCategory> getChildren(Long parentId) {
        LambdaQueryWrapper<MaterialCategory> wrapper = new LambdaQueryWrapper<>();
        if (parentId == null) {
            parentId = 0L;
        }
        wrapper.eq(MaterialCategory::getParentId, parentId);
        wrapper.orderByAsc(MaterialCategory::getSortOrder);
        return materialCategoryMapper.selectList(wrapper);
    }
}
