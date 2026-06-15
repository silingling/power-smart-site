package com.powersmart.hazard.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.common.util.PageHelper;
import com.powersmart.hazard.entity.SafetyMaterial;
import com.powersmart.hazard.entity.SafetyMaterialCatalog;
import com.powersmart.hazard.mapper.SafetyMaterialCatalogMapper;
import com.powersmart.hazard.mapper.SafetyMaterialMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 质量资料 + 质量资料目录 通用服务层
 * 复用安全资料的存储表 (safety_material) 和目录表 (safety_material_catalog)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QualMaterialService {

    private final SafetyMaterialMapper safetyMaterialMapper;
    private final SafetyMaterialCatalogMapper safetyMaterialCatalogMapper;

    // ==================== 质量资料 ====================

    public PageResult<SafetyMaterial> selectPageList(Map<String, Object> params) {
        Page<SafetyMaterial> page = PageHelper.of(params);
        LambdaQueryWrapper<SafetyMaterial> wrapper = buildWrapper(params);
        return PageResult.from(safetyMaterialMapper.selectPage(page, wrapper));
    }

    public List<SafetyMaterial> selectByCollect(Long projectId) {
        LambdaQueryWrapper<SafetyMaterial> w = new LambdaQueryWrapper<SafetyMaterial>()
                .eq(SafetyMaterial::getProjectId, projectId)
                .eq(SafetyMaterial::getIsCollect, 1)
                .orderByDesc(SafetyMaterial::getCreateTime);
        return safetyMaterialMapper.selectList(w);
    }

    public void add(SafetyMaterial entity) {
        safetyMaterialMapper.insert(entity);
    }

    public SafetyMaterial getById(Long id) {
        return safetyMaterialMapper.selectById(id);
    }

    public void removeById(Long id) {
        safetyMaterialMapper.deleteById(id);
    }

    public void collect(Long id) {
        SafetyMaterial m = safetyMaterialMapper.selectById(id);
        if (m != null) {
            m.setIsCollect(1);
            safetyMaterialMapper.updateById(m);
        }
    }

    public void deleteCollect(Long id) {
        SafetyMaterial m = safetyMaterialMapper.selectById(id);
        if (m != null) {
            m.setIsCollect(0);
            safetyMaterialMapper.updateById(m);
        }
    }

    private LambdaQueryWrapper<SafetyMaterial> buildWrapper(Map<String, Object> params) {
        LambdaQueryWrapper<SafetyMaterial> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SafetyMaterial::getIsQual, 1); // qual 标识
        if (params != null) {
            if (params.containsKey("projectId") && params.get("projectId") != null)
                wrapper.eq(SafetyMaterial::getProjectId, Long.valueOf(params.get("projectId").toString()));
            if (params.containsKey("catalogId") && params.get("catalogId") != null)
                wrapper.eq(SafetyMaterial::getCatalogId, Long.valueOf(params.get("catalogId").toString()));
        }
        wrapper.orderByDesc(SafetyMaterial::getCreateTime);
        return wrapper;
    }

    // ==================== 质量资料目录 ====================

    public void addCatalog(SafetyMaterialCatalog entity) {
        safetyMaterialCatalogMapper.insert(entity);
    }

    public List<Map<String, Object>> selectCatalogTree(Long projectId) {
        List<Map<String, Object>> result = new ArrayList<>();
        List<SafetyMaterialCatalog> roots = safetyMaterialCatalogMapper.selectList(
                new LambdaQueryWrapper<SafetyMaterialCatalog>()
                        .eq(SafetyMaterialCatalog::getProjectId, projectId)
                        .eq(SafetyMaterialCatalog::getParentId, 0));
        for (SafetyMaterialCatalog r : roots) {
            Map<String, Object> node = catalogToMap(r);
            node.put("children", buildChildren(r.getId()));
            result.add(node);
        }
        return result;
    }

    public void deleteCatalog(Long id) {
        safetyMaterialCatalogMapper.deleteById(id);
    }

    public SafetyMaterialCatalog getCatalogById(Long id) {
        return safetyMaterialCatalogMapper.selectById(id);
    }

    private List<Map<String, Object>> buildChildren(Long parentId) {
        List<Map<String, Object>> children = new ArrayList<>();
        List<SafetyMaterialCatalog> list = safetyMaterialCatalogMapper.selectList(
                new LambdaQueryWrapper<SafetyMaterialCatalog>().eq(SafetyMaterialCatalog::getParentId, parentId));
        for (SafetyMaterialCatalog c : list) {
            Map<String, Object> node = catalogToMap(c);
            node.put("children", buildChildren(c.getId()));
            children.add(node);
        }
        return children;
    }

    private Map<String, Object> catalogToMap(SafetyMaterialCatalog c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.getId());
        m.put("name", c.getName());
        m.put("parentId", c.getParentId());
        m.put("projectId", c.getProjectId());
        return m;
    }
}
