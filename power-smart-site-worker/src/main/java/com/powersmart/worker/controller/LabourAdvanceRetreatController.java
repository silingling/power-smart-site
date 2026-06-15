package com.powersmart.worker.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.worker.entity.LabourAdvanceRetreat;
import com.powersmart.worker.mapper.LabourAdvanceRetreatMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 工人进出场记录 — 对接萤丰前端 build/labourAdvanceRetreat/*
 */
@RestController
@RequestMapping("/build/labourAdvanceRetreat")
@RequiredArgsConstructor
public class LabourAdvanceRetreatController {

    private final LabourAdvanceRetreatMapper mapper;

    @SuppressWarnings("unchecked")
    private Page<LabourAdvanceRetreat> extractPage(Map<String, Object> params) {
        int p = 1, s = 20;
        if (params != null) {
            if (params.containsKey("page")) p = Integer.parseInt(params.get("page").toString());
            if (params.containsKey("limit")) s = Integer.parseInt(params.get("limit").toString());
            if (params.containsKey("pageSize")) s = Integer.parseInt(params.get("pageSize").toString());
        }
        return new Page<>(p, s);
    }

    @PostMapping("/selectPageList")
    public Result<PageResult<LabourAdvanceRetreat>> selectPageList(@RequestBody(required = false) Map<String, Object> params) {
        Page<LabourAdvanceRetreat> page = extractPage(params);
        LambdaQueryWrapper<LabourAdvanceRetreat> wrapper = buildWrapper(params);
        return Result.ok(PageResult.from(mapper.selectPage(page, wrapper)));
    }

    @PostMapping("/getById/{id}")
    public Result<LabourAdvanceRetreat> getById(@PathVariable Long id) {
        return Result.ok(mapper.selectById(id));
    }

    @PostMapping("/save")
    public Result<Void> save(@RequestBody LabourAdvanceRetreat entity) {
        if (entity.getId() != null) {
            mapper.updateById(entity);
        } else {
            mapper.insert(entity);
        }
        return Result.ok();
    }

    @PostMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        mapper.deleteById(id);
        return Result.ok();
    }

    @SuppressWarnings("unchecked")
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
