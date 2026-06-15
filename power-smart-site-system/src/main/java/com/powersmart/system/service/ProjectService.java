package com.powersmart.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.powersmart.system.entity.Project;
import com.powersmart.system.mapper.ProjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ProjectService {

    private final ProjectMapper projectMapper;

    public void create(Project project) {
        projectMapper.insert(project);
    }

    public List<Project> list(Integer status) {
        return projectMapper.selectList(
                new LambdaQueryWrapper<Project>()
                        .eq(status != null, Project::getStatus, status));
    }

    public Project getById(Long id) {
        return projectMapper.selectById(id);
    }

    public void update(Project project) {
        projectMapper.updateById(project);
    }
}
