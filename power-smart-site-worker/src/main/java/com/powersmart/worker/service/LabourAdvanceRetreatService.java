package com.powersmart.worker.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.entity.PageResult;
import com.powersmart.worker.entity.LabourAdvanceRetreat;
import com.powersmart.worker.mapper.LabourAdvanceRetreatMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 工人进出场记录 Service — 提取 LabourAdvanceRetreatController 的业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LabourAdvanceRetreatService {

    private final LabourAdvanceRetreatMapper mapper;

    /**
     * 分页查询
     */
    public Page<LabourAdvanceRetreat> selectPageList(Map<String, Object> params) {
        Page<LabourAdvanceRetreat> page = extractPage(params);
        return mapper.selectPage(page, buildWrapper(params));
    }

    /**
     * 根据 ID 查询
     */
    public LabourAdvanceRetreat getById(Long id) {
        return mapper.selectById(id);
    }

    /**
     * 新增或更新
     */
    public void save(LabourAdvanceRetreat entity) {
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
    private Page<LabourAdvanceRetreat> extractPage(Map<String, Object> params) {
        int p = 1, s = 20;
        if (params != null) {
            if (params.get("page") != null) try { p = Integer.parseInt(params.get("page").toString()); } catch (NumberFormatException ignored) {}
            if (params.get("limit") != null) try { s = Integer.parseInt(params.get("limit").toString()); } catch (NumberFormatException ignored) {}
            if (params.get("pageSize") != null) try { s = Integer.parseInt(params.get("pageSize").toString()); } catch (NumberFormatException ignored) {}
        }
        return new Page<>(p, s);
    }

    private LambdaQueryWrapper<LabourAdvanceRetreat> buildWrapper(Map<String, Object> params) {
        LambdaQueryWrapper<LabourAdvanceRetreat> wrapper = new LambdaQueryWrapper<>();
        if (params != null) {
            if (params.containsKey("projectId") && params.get("projectId") != null)
                wrapper.eq(LabourAdvanceRetreat::getProjectId, Long.valueOf(params.get("projectId").toString()));
            if (params.containsKey("workerId") && params.get("workerId") != null)
                wrapper.eq(LabourAdvanceRetreat::getWorkerId, Long.valueOf(params.get("workerId").toString()));
            if (params.containsKey("status") && params.get("status") != null)
                wrapper.eq(LabourAdvanceRetreat::getStatus, Integer.parseInt(params.get("status").toString()));
            if (params.containsKey("workerName") && params.get("workerName") != null)
                wrapper.like(LabourAdvanceRetreat::getWorkerName, params.get("workerName").toString());
            if (params.containsKey("idCard") && params.get("idCard") != null)
                wrapper.like(LabourAdvanceRetreat::getIdCard, params.get("idCard").toString());
        }
        wrapper.orderByDesc(LabourAdvanceRetreat::getCreatedAt);
        return wrapper;
    }
}
