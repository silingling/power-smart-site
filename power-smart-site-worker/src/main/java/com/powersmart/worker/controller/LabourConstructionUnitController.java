package com.powersmart.worker.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.worker.entity.LabourConstructionUnit;
import com.powersmart.worker.service.LabourConstructionUnitService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 建设单位管理 — 对接同业电力（tongye）前端 build/labourConstructionUnit/*
 */
@RestController
@RequestMapping("/build/labourConstructionUnit")
@RequiredArgsConstructor
public class LabourConstructionUnitController {

    private final LabourConstructionUnitService labourConstructionUnitService;

    @PostMapping("/selectPageList")
    public Result<PageResult<LabourConstructionUnit>> selectPageList(@RequestBody(required = false) Map<String, Object> params) {
        Page<LabourConstructionUnit> page = labourConstructionUnitService.selectPageList(params);
        return Result.ok(PageResult.from(page));
    }

    @PostMapping("/getById/{id}")
    public Result<LabourConstructionUnit> getById(@PathVariable Long id) {
        return Result.ok(labourConstructionUnitService.getById(id));
    }

    @PostMapping("/save")
    public Result<Void> save(@RequestBody LabourConstructionUnit entity) {
        labourConstructionUnitService.save(entity);
        return Result.ok();
    }

    @PostMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        labourConstructionUnitService.delete(id);
        return Result.ok();
    }
}
