package com.powersmart.worker.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.worker.entity.LabourSubcontractor;
import com.powersmart.worker.service.LabourSubcontractorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/build/labourSubcontractor")
@RequiredArgsConstructor
public class LabourSubcontractorController {

    private final LabourSubcontractorService labourSubcontractorService;

    @PostMapping("/list")
    public Result<PageResult<LabourSubcontractor>> list(@RequestBody(required = false) Map<String, Object> params) {
        Page<LabourSubcontractor> page = labourSubcontractorService.selectPageList(params);
        return Result.ok(PageResult.from(page));
    }

    @PostMapping("/selectPageList")
    public Result<PageResult<LabourSubcontractor>> selectPageList(@RequestBody(required = false) Map<String, Object> params) {
        Page<LabourSubcontractor> page = labourSubcontractorService.selectPageList(params);
        return Result.ok(PageResult.from(page));
    }

    @PostMapping("/queryById/{id}")
    public Result<LabourSubcontractor> queryById(@PathVariable Long id) {
        return Result.ok(labourSubcontractorService.getById(id));
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody LabourSubcontractor entity) {
        labourSubcontractorService.add(entity);
        return Result.ok();
    }

    @PostMapping("/edit")
    public Result<Void> edit(@RequestBody LabourSubcontractor entity) {
        labourSubcontractorService.update(entity);
        return Result.ok();
    }

    @PostMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        labourSubcontractorService.delete(id);
        return Result.ok();
    }

    @PostMapping("/selcetIdsAndName")
    public Result<List<Map<String, Object>>> selcetIdsAndName(@RequestBody(required = false) Map<String, Object> params) {
        return Result.ok(labourSubcontractorService.selectIdsAndName(params));
    }
}
