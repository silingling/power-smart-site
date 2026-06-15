package com.powersmart.worker.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.worker.entity.LabourConstructionUnit;
import com.powersmart.worker.mapper.LabourConstructionUnitMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 建设单位 Service — 提取 LabourConstructionUnitController 的业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LabourConstructionUnitService {

    private final LabourConstructionUnitMapper mapper;

    /**
     * 分页查询
     */
    public Page<LabourConstructionUnit> selectPageList(Map<String, Object> params) {
        Page<LabourConstructionUnit> page = extractPage(params);
        LambdaQueryWrapper<LabourConstructionUnit> wrapper = new LambdaQueryWrapper<>();
        if (params != null) {
            if (params.containsKey("unitName")) {
                wrapper.like(LabourConstructionUnit::getUnitName, params.get("unitName"));
            }
        }
        wrapper.orderByDesc(LabourConstructionUnit::getCreatedAt);
        return mapper.selectPage(page, wrapper);
    }

    /**
     * 根据 ID 查询
     */
    public LabourConstructionUnit getById(Long id) {
        return mapper.selectById(id);
    }

    /**
     * 新增或更新
     */
    public void save(LabourConstructionUnit entity) {
        if (entity.getId() != null) {
            mapper.updateById(entity);
        } else {
            mapper.insert(entity);
        }
    }

    /**
     * 删除
     */
    public void delete(Long id) {
        mapper.deleteById(id);
    }

    @SuppressWarnings("unchecked")
    private Page<LabourConstructionUnit> extractPage(Map<String, Object> params) {
        int p = 1, s = 20;
        if (params != null) {
            if (params.get("page") != null) try { p = Integer.parseInt(params.get("page").toString()); } catch (NumberFormatException ignored) {}
            if (params.get("limit") != null) try { s = Integer.parseInt(params.get("limit").toString()); } catch (NumberFormatException ignored) {}
            if (params.get("pageSize") != null) try { s = Integer.parseInt(params.get("pageSize").toString()); } catch (NumberFormatException ignored) {}
        }
        return new Page<>(p, s);
    }
}
