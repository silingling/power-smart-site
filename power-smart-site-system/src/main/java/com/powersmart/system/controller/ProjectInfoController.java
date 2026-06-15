package com.powersmart.system.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.system.entity.Project;
import com.powersmart.system.mapper.ProjectMapper;
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

    private final ProjectMapper projectMapper;

    @PostMapping("/queryPageList")
    public Result<PageResult<Map<String, Object>>> queryPageList(@RequestBody(required = false) Map<String, Object> params) {
        int p = 1, s = 20;
        if (params != null) {
            try { if (params.get("page") != null) p = Integer.parseInt(params.get("page").toString()); } catch (Exception ignored) {}
            try { if (params.get("pageSize") != null) s = Math.min(Integer.parseInt(params.get("pageSize").toString()), 200); } catch (Exception ignored) {}
            try { if (params.get("limit") != null) s = Math.min(Integer.parseInt(params.get("limit").toString()), 200); } catch (Exception ignored) {}
        }
        Page<Project> page = new Page<>(p, s);
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        if (params != null) {
            if (params.containsKey("projectName") && params.get("projectName") != null)
                wrapper.like(Project::getProjectName, params.get("projectName").toString());
            if (params.containsKey("status") && params.get("status") != null)
                wrapper.eq(Project::getStatus, Integer.parseInt(params.get("status").toString()));
        }
        wrapper.orderByDesc(Project::getCreatedAt);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Project proj : projectMapper.selectPage(page, wrapper).getRecords()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", proj.getId());
            m.put("projectName", proj.getProjectName());
            m.put("projectCode", proj.getProjectCode());
            m.put("projectType", proj.getProjectType());
            m.put("projectAddress", proj.getProjectAddress());
            m.put("contractor", proj.getContractor());
            m.put("supervisor", proj.getSupervisor());
            m.put("planStartDate", proj.getPlanStartDate());
            m.put("planEndDate", proj.getPlanEndDate());
            m.put("status", proj.getStatus());
            list.add(m);
        }
        return Result.ok(PageResult.of(list, page.getTotal(), p, s));
    }

    @PostMapping("/queryById/{id}")
    public Result<Map<String, Object>> queryById(@PathVariable Long id) {
        Project proj = projectMapper.selectById(id);
        if (proj == null) return Result.fail("项目不存在");
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", proj.getId());
        m.put("projectName", proj.getProjectName());
        m.put("projectCode", proj.getProjectCode());
        m.put("projectType", proj.getProjectType());
        m.put("projectAddress", proj.getProjectAddress());
        m.put("contractor", proj.getContractor());
        m.put("supervisor", proj.getSupervisor());
        m.put("planStartDate", proj.getPlanStartDate());
        m.put("planEndDate", proj.getPlanEndDate());
        m.put("status", proj.getStatus());
        return Result.ok(m);
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody Project entity) {
        projectMapper.insert(entity);
        return Result.ok();
    }

    @PostMapping("/edit")
    public Result<Void> edit(@RequestBody Project entity) {
        projectMapper.updateById(entity);
        return Result.ok();
    }

    @PostMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        projectMapper.deleteById(id);
        return Result.ok();
    }

    @PostMapping("/projectUser/list")
    public Result<List<Map<String, Object>>> projectUserList(@RequestBody Map<String, Object> params) {
        // 项目用户关联 - 返回空列表，按需扩展
        return Result.ok(new ArrayList<>());
    }
}
