package com.powersmart.worker.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.entity.PageResult;
import com.powersmart.worker.entity.LabourSubcontractor;
import com.powersmart.worker.mapper.LabourSubcontractorMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 分包商 Service — 提取 LabourSubcontractorController 的业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LabourSubcontractorService {

    private final LabourSubcontractorMapper mapper;

    /**
     * 分页查询
     */
    public Page<LabourSubcontractor> selectPageList(Map<String, Object> params) {
        Page<LabourSubcontractor> page = extractPage(params);
        return mapper.selectPage(page, buildWrapper(params));
    }

    /**
     * 根据 ID 查询
     */
    public LabourSubcontractor getById(Long id) {
        return mapper.selectById(id);
    }

    /**
     * 新增
     */
    public void add(LabourSubcontractor entity) {
        mapper.insert(entity);
    }

    /**
     * 编辑
     */
    public void update(LabourSubcontractor entity) {
        mapper.updateById(entity);
    }

    /**
     * 删除
     */
    public void delete(Long id) {
        mapper.deleteById(id);
    }

    /**
     * 查询 id + subcontractorName 列表（仅启用的分包商）
     */
    public List<Map<String, Object>> selectIdsAndName(Map<String, Object> params) {
        LambdaQueryWrapper<LabourSubcontractor> wrapper = new LambdaQueryWrapper<>();
        if (params != null && params.containsKey("projectId") && params.get("projectId") != null)
            wrapper.eq(LabourSubcontractor::getProjectId, Long.valueOf(params.get("projectId").toString()));
        wrapper.eq(LabourSubcontractor::getStatus, 1);
        List<LabourSubcontractor> list = mapper.selectList(wrapper);
        return list.stream().map(s -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", s.getId());
            m.put("subcontractorName", s.getSubcontractorName());
            return m;
        }).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private Page<LabourSubcontractor> extractPage(Map<String, Object> params) {
        int p = 1, s = 20;
        if (params != null) {
            if (params.get("page") != null) try { p = Integer.parseInt(params.get("page").toString()); } catch (NumberFormatException ignored) {}
            if (params.get("limit") != null) try { s = Integer.parseInt(params.get("limit").toString()); } catch (NumberFormatException ignored) {}
            if (params.get("pageSize") != null) try { s = Integer.parseInt(params.get("pageSize").toString()); } catch (NumberFormatException ignored) {}
        }
        return new Page<>(p, s);
    }

    private LambdaQueryWrapper<LabourSubcontractor> buildWrapper(Map<String, Object> params) {
        LambdaQueryWrapper<LabourSubcontractor> wrapper = new LambdaQueryWrapper<>();
        if (params != null) {
            if (params.containsKey("projectId") && params.get("projectId") != null)
                wrapper.eq(LabourSubcontractor::getProjectId, Long.valueOf(params.get("projectId").toString()));
            if (params.containsKey("subcontractorName") && params.get("subcontractorName") != null)
                wrapper.like(LabourSubcontractor::getSubcontractorName, params.get("subcontractorName").toString());
            if (params.containsKey("status") && params.get("status") != null)
                wrapper.eq(LabourSubcontractor::getStatus, Integer.parseInt(params.get("status").toString()));
        }
        wrapper.orderByDesc(LabourSubcontractor::getCreatedAt);
        return wrapper;
    }
}
