package com.powersmart.system.controller;

import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.system.entity.Project;
import com.powersmart.system.service.ProjectInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 项目信息管理 — 同业电力前端 build/projectInfo/*
 */
@RestController
@RequestMapping("/build/projectInfo")
@RequiredArgsConstructor
public class ProjectInfoController {

    private final ProjectInfoService projectInfoService;

    @PostMapping("/queryPageList")
    public Result<PageResult<Map<String, Object>>> queryPageList(@RequestBody(required = false) Map<String, Object> params) {
        return Result.ok(projectInfoService.queryPageList(params));
    }

    @PostMapping("/queryById/{id}")
    public Result<Map<String, Object>> queryById(@PathVariable Long id) {
        Map<String, Object> data = projectInfoService.queryById(id);
        if (data == null) return Result.fail("项目不存在");
        return Result.ok(data);
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody Project entity) {
        projectInfoService.add(entity);
        return Result.ok();
    }

    @PostMapping("/edit")
    public Result<Void> edit(@RequestBody Project entity) {
        projectInfoService.edit(entity);
        return Result.ok();
    }

    @PostMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        projectInfoService.delete(id);
        return Result.ok();
    }

    @PostMapping("/updateProjectInfo")
    public Result<Void> updateProjectInfo(@RequestBody Project entity) {
        projectInfoService.updateProjectInfo(entity);
        return Result.ok();
    }

    @PostMapping("/getProjectInfoById/{id}")
    public Result<Map<String, Object>> getProjectInfoById(@PathVariable Long id) {
        return queryById(id);
    }

    @PostMapping("/queryPageListAll")
    public Result<PageResult<Map<String, Object>>> queryPageListAll(@RequestBody(required = false) Map<String, Object> params) {
        return queryPageList(params);
    }

    @PostMapping("/getThree/{projectId}")
    public Result<Map<String, Object>> getThree(@PathVariable Long projectId) {
        return Result.ok(projectInfoService.getThree(projectId));
    }

    @PostMapping("/queryPageListByEval")
    public Result<PageResult<Map<String, Object>>> queryPageListByEval(@RequestBody(required = false) Map<String, Object> params) {
        return queryPageList(params);
    }

    @GetMapping("/sync")
    public Result<Void> sync() {
        // 数据同步接口 - 按需实现
        return Result.ok();
    }

    @PostMapping("/projectUser/list")
    public Result<List<Map<String, Object>>> projectUserList(@RequestBody Map<String, Object> params) {
        return Result.ok(projectInfoService.projectUserList(params));
    }
}
