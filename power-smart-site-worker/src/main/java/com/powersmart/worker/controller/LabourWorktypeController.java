package com.powersmart.worker.controller;

import com.powersmart.common.entity.Result;
import com.powersmart.worker.entity.LabourWorktype;
import com.powersmart.worker.service.LabourWorktypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 工种字典 — 同业电力前端 build/labourWorktype/*
 */
@RestController
@RequestMapping("/build/labourWorktype")
@RequiredArgsConstructor
public class LabourWorktypeController {

    private final LabourWorktypeService labourWorktypeService;

    @PostMapping("/selcetIdsAndWorktype")
    public Result<List<Map<String, Object>>> selcetIdsAndWorktype() {
        return Result.ok(labourWorktypeService.selectIdsAndWorktype());
    }

    @PostMapping("/selcetIdsAndName")
    public Result<List<Map<String, Object>>> selcetIdsAndName() {
        return Result.ok(labourWorktypeService.selectIdsAndName());
    }

    @PostMapping("/list")
    public Result<List<LabourWorktype>> list() {
        return Result.ok(labourWorktypeService.listAll());
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody LabourWorktype entity) {
        labourWorktypeService.add(entity);
        return Result.ok();
    }

    @PostMapping("/edit")
    public Result<Void> edit(@RequestBody LabourWorktype entity) {
        labourWorktypeService.update(entity);
        return Result.ok();
    }

    @PostMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        labourWorktypeService.delete(id);
        return Result.ok();
    }
}
