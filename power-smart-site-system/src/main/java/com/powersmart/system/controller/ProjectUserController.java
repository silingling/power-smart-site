package com.powersmart.system.controller;

import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 项目用户关联 — 同业电力前端 build/projectUser/*
 * 无独立数据库表，逻辑关联 project 表，暂为桩实现
 */
@RestController
@RequestMapping("/build/projectUser")
@RequiredArgsConstructor
public class ProjectUserController {

    @PostMapping("/saveUsers")
    public Result<Void> saveUsers(@RequestBody Map<String, Object> params) {
        // TODO: 实现项目用户保存逻辑
        return Result.ok();
    }

    @PostMapping("/delUser/{id}")
    public Result<Void> delUser(@PathVariable Long id) {
        // TODO: 实现项目用户删除逻辑
        return Result.ok();
    }

    @PostMapping("/queryPageListAll")
    public Result<PageResult<Map<String, Object>>> queryPageListAll(@RequestBody(required = false) Map<String, Object> params) {
        return Result.ok(PageResult.of(new ArrayList<>(), 0, 1, 20));
    }

    @PostMapping("/queryPageList")
    public Result<PageResult<Map<String, Object>>> queryPageList(@RequestBody(required = false) Map<String, Object> params) {
        return Result.ok(PageResult.of(new ArrayList<>(), 0, 1, 20));
    }
}
