package com.powersmart.hazard.service;

import cn.hutool.core.util.StrUtil;
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 安全资料 + 安全资料目录 通用服务层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SafetyMaterialService {

    private final SafetyMaterialMapper safetyMaterialMapper;
    private final SafetyMaterialCatalogMapper safetyMaterialCatalogMapper;

    // ==================== 安全资料 ====================

    public PageResult<SafetyMaterial> selectPageAllByPid(Map<String, Object> params) {
        Page<SafetyMaterial> page = PageHelper.of(params);
        LambdaQueryWrapper<SafetyMaterial> wrapper = new LambdaQueryWrapper<>();
        if (params != null) {
            if (params.containsKey("projectId") && params.get("projectId") != null)
                wrapper.eq(SafetyMaterial::getProjectId, Long.valueOf(params.get("projectId").toString()));
            if (params.containsKey("catalogId") && params.get("catalogId") != null)
                wrapper.eq(SafetyMaterial::getCatalogId, Long.valueOf(params.get("catalogId").toString()));
        }
        wrapper.orderByDesc(SafetyMaterial::getCreatedAt);
        return PageResult.from(safetyMaterialMapper.selectPage(page, wrapper));
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

    public PageResult<SafetyMaterial> selectByCollect(Long projectId) {
        Page<SafetyMaterial> page = new Page<>(1, 20);
        LambdaQueryWrapper<SafetyMaterial> wrapper = new LambdaQueryWrapper<SafetyMaterial>()
                .eq(SafetyMaterial::getProjectId, projectId)
                .eq(SafetyMaterial::getIsCollect, 1);
        return PageResult.from(safetyMaterialMapper.selectPage(page, wrapper));
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

    // ==================== 安全资料目录 ====================

    public void addCatalog(SafetyMaterialCatalog entity) {
        safetyMaterialCatalogMapper.insert(entity);
    }

    public List<SafetyMaterialCatalog> selectCatalogTree(Long projectId) {
        return safetyMaterialCatalogMapper.selectList(
                new LambdaQueryWrapper<SafetyMaterialCatalog>()
                        .eq(SafetyMaterialCatalog::getProjectId, projectId)
                        .orderByAsc(SafetyMaterialCatalog::getSortOrder));
    }

    public SafetyMaterialCatalog getCatalogById(Long id) {
        return safetyMaterialCatalogMapper.selectById(id);
    }

    public void deleteCatalog(Long id) {
        safetyMaterialCatalogMapper.deleteById(id);
    }

    public void deleteCatalogWithChildren(Long id) {
        safetyMaterialCatalogMapper.deleteById(id);
        safetyMaterialCatalogMapper.delete(new LambdaQueryWrapper<SafetyMaterialCatalog>()
                .eq(SafetyMaterialCatalog::getParentId, id));
    }

    // ==================== 质量资料目录（复用安全资料目录表） ====================

    public List<Map<String, Object>> selectQualCatalogTree(Long projectId) {
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
