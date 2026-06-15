package com.powersmart.system.controller;

import com.powersmart.common.entity.Result;
import com.powersmart.system.entity.Project;
import com.powersmart.system.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public Result<Project> create(@RequestBody Project project) {
        projectService.create(project);
        return Result.ok(project);
    }

    @GetMapping
    public Result<List<Project>> list(@RequestParam(required = false) Integer status) {
        return Result.ok(projectService.list(status));
    }

    @GetMapping("/{id}")
    public Result<Project> getById(@PathVariable Long id) {
        return Result.ok(projectService.getById(id));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Project project) {
        project.setId(id);
        projectService.update(project);
        return Result.ok();
    }
}
