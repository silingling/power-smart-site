package com.powersmart.worker.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.worker.entity.LabourAdvanceRetreat;
import com.powersmart.worker.service.LabourAdvanceRetreatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 工人进出场记录 — 对接同业电力（tongye）前端 build/labourAdvanceRetreat/*
 */
@RestController
@RequestMapping("/build/labourAdvanceRetreat")
@RequiredArgsConstructor
public class LabourAdvanceRetreatController {

    private final LabourAdvanceRetreatService labourAdvanceRetreatService;

    @PostMapping("/selectPageList")
    public Result<PageResult<LabourAdvanceRetreat>> selectPageList(@RequestBody(required = false) Map<String, Object> params) {
        Page<LabourAdvanceRetreat> page = labourAdvanceRetreatService.selectPageList(params);
        return Result.ok(PageResult.from(page));
    }

    @PostMapping("/getById/{id}")
    public Result<LabourAdvanceRetreat> getById(@PathVariable Long id) {
        return Result.ok(labourAdvanceRetreatService.getById(id));
    }

    @PostMapping("/save")
    public Result<Void> save(@RequestBody LabourAdvanceRetreat entity) {
        labourAdvanceRetreatService.save(entity);
        return Result.ok();
    }

    @PostMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        labourAdvanceRetreatService.delete(id);
        return Result.ok();
    }
}
