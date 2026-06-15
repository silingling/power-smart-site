package com.powersmart.worker.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.powersmart.common.entity.Result;
import com.powersmart.worker.entity.LabourWorktype;
import com.powersmart.worker.mapper.LabourWorktypeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 工种字典 — 同业电力前端 build/labourWorktype/*
 */
@RestController
@RequestMapping("/build/labourWorktype")
@RequiredArgsConstructor
public class LabourWorktypeController {

    private final LabourWorktypeMapper worktypeMapper;

    @PostMapping("/selcetIdsAndWorktype")
    public Result<List<Map<String, Object>>> selcetIdsAndWorktype() {
        List<LabourWorktype> list = worktypeMapper.selectList(
                new LambdaQueryWrapper<LabourWorktype>()
                        .eq(LabourWorktype::getStatus, 1)
                        .orderByAsc(LabourWorktype::getSortOrder));
        return Result.ok(list.stream().map(w -> {
            Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("id", w.getId());
            m.put("worktypeName", w.getWorktypeName());
            m.put("worktypeCode", w.getWorktypeCode());
            return m;
        }).collect(Collectors.toList()));
    }

    @PostMapping("/selcetIdsAndName")
    public Result<List<Map<String, Object>>> selcetIdsAndName() {
        List<LabourWorktype> list = worktypeMapper.selectList(
                new LambdaQueryWrapper<LabourWorktype>()
                        .eq(LabourWorktype::getStatus, 1)
                        .orderByAsc(LabourWorktype::getSortOrder));
        return Result.ok(list.stream().map(w -> {
            Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("id", w.getId());
            m.put("name", w.getWorktypeName());
            return m;
        }).collect(Collectors.toList()));
    }

    @PostMapping("/list")
    public Result<List<LabourWorktype>> list() {
        return Result.ok(worktypeMapper.selectList(
                new LambdaQueryWrapper<LabourWorktype>()
                        .orderByAsc(LabourWorktype::getSortOrder)));
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody LabourWorktype entity) {
        worktypeMapper.insert(entity);
        return Result.ok();
    }

    @PostMapping("/edit")
    public Result<Void> edit(@RequestBody LabourWorktype entity) {
        worktypeMapper.updateById(entity);
        return Result.ok();
    }

    @PostMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        worktypeMapper.deleteById(id);
        return Result.ok();
    }
}
