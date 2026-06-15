package com.powersmart.system.controller;

import com.powersmart.common.entity.Result;
import com.powersmart.system.entity.Project;
import com.powersmart.system.mapper.ProjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectMapper projectMapper;

    @PostMapping
    public Result<Project> create(@RequestBody Project project) {
        projectMapper.insert(project);
        return Result.ok(project);
    }

    @GetMapping
    public Result<List<Project>> list(@RequestParam(required = false) Integer status) {
        return Result.ok(projectMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Project>()
                        .eq(status != null, Project::getStatus, status)));
    }

    @GetMapping("/{id}")
    public Result<Project> getById(@PathVariable Long id) {
        return Result.ok(projectMapper.selectById(id));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Project project) {
        project.setId(id);
        projectMapper.updateById(project);
        return Result.ok();
    }
}
