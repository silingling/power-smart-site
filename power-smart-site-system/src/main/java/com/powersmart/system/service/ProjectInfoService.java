package com.powersmart.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.entity.PageResult;
import com.powersmart.system.entity.Project;
import com.powersmart.system.mapper.ProjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 项目信息服务 — 封装 ProjectInfoController 的业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectInfoService {

    private final ProjectMapper projectMapper;

    /**
     * 分页查询项目列表
     */
    public PageResult<Map<String, Object>> queryPageList(Map<String, Object> params) {
        int p = 1, s = 20;
        if (params != null) {
            try {
                if (params.get("page") != null) p = Integer.parseInt(params.get("page").toString());
            } catch (Exception ignored) {}
            try {
                if (params.get("pageSize") != null) s = Math.min(Integer.parseInt(params.get("pageSize").toString()), 200);
            } catch (Exception ignored) {}
            try {
                if (params.get("limit") != null) s = Math.min(Integer.parseInt(params.get("limit").toString()), 200);
            } catch (Exception ignored) {}
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
        return PageResult.of(list, page.getTotal(), p, s);
    }

    /**
     * 根据 ID 查询项目详情
     */
    public Map<String, Object> queryById(Long id) {
        Project proj = projectMapper.selectById(id);
        if (proj == null) return null;
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
        return m;
    }

    /**
     * 新增项目
     */
    public void add(Project entity) {
        projectMapper.insert(entity);
    }

    /**
     * 编辑项目
     */
    public void edit(Project entity) {
        projectMapper.updateById(entity);
    }

    /**
     * 删除项目
     */
    public void delete(Long id) {
        projectMapper.deleteById(id);
    }

    /**
     * 更新项目信息
     */
    public void updateProjectInfo(Project entity) {
        projectMapper.updateById(entity);
    }

    /**
     * 获取项目统计概览（楼栋数、设备数、工人数）
     */
    public Map<String, Object> getThree(Long projectId) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("buildingCount", 0);  // 楼栋数
        data.put("deviceCount", 0);    // 设备数
        data.put("workerCount", 0);    // 工人数
        return data;
    }

    /**
     * 项目用户关联列表
     */
    public List<Map<String, Object>> projectUserList(Map<String, Object> params) {
        return new ArrayList<>();
    }
}
