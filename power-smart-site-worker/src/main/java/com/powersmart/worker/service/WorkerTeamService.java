package com.powersmart.worker.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.entity.PageResult;
import com.powersmart.worker.entity.WorkerTeam;
import com.powersmart.worker.mapper.WorkerTeamMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 施工班组 Service — 合并 LabourTeamController + WorkerTeamController 的业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkerTeamService {

    private final WorkerTeamMapper teamMapper;

    /**
     * 分页查询（build/labourTeam/list）
     */
    public Page<WorkerTeam> queryPage(Map<String, Object> params) {
        Page<WorkerTeam> page = extractPage(params);
        LambdaQueryWrapper<WorkerTeam> wrapper = new LambdaQueryWrapper<>();
        if (params != null) {
            if (params.containsKey("projectId") && params.get("projectId") != null)
                wrapper.eq(WorkerTeam::getProjectId, Long.valueOf(params.get("projectId").toString()));
            if (params.containsKey("teamName") && params.get("teamName") != null)
                wrapper.like(WorkerTeam::getTeamName, params.get("teamName").toString());
        }
        wrapper.orderByDesc(WorkerTeam::getCreatedAt);
        return teamMapper.selectPage(page, wrapper);
    }

    /**
     * 根据 ID 查询
     */
    public WorkerTeam getById(Long id) {
        return teamMapper.selectById(id);
    }

    /**
     * 新增
     */
    public void add(WorkerTeam team) {
        teamMapper.insert(team);
    }

    /**
     * 编辑
     */
    public void update(WorkerTeam team) {
        teamMapper.updateById(team);
    }

    /**
     * 删除
     */
    public void delete(Long id) {
        teamMapper.deleteById(id);
    }

    /**
     * 查询 id + teamName 列表（仅启用的班组）
     */
    public List<Map<String, Object>> selectIdsAndName(Map<String, Object> params) {
        LambdaQueryWrapper<WorkerTeam> wrapper = new LambdaQueryWrapper<>();
        if (params != null && params.containsKey("projectId") && params.get("projectId") != null)
            wrapper.eq(WorkerTeam::getProjectId, Long.valueOf(params.get("projectId").toString()));
        wrapper.eq(WorkerTeam::getStatus, 1);
        List<WorkerTeam> list = teamMapper.selectList(wrapper);
        return list.stream().map(t -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", t.getId());
            m.put("teamName", t.getTeamName());
            return m;
        }).collect(Collectors.toList());
    }

    // === WorkerTeamController 方法 ===

    /**
     * 创建班组（/api/v1/teams POST）
     */
    public WorkerTeam create(WorkerTeam team) {
        teamMapper.insert(team);
        return team;
    }

    /**
     * 按项目查询班组列表（/api/v1/teams GET）
     */
    public List<WorkerTeam> listByProject(Long projectId) {
        return teamMapper.selectList(
                new LambdaQueryWrapper<WorkerTeam>()
                        .eq(WorkerTeam::getProjectId, projectId));
    }

    @SuppressWarnings("unchecked")
    private Page<WorkerTeam> extractPage(Map<String, Object> params) {
        int p = 1, s = 20;
        if (params != null) {
            if (params.get("page") != null) try { p = Integer.parseInt(params.get("page").toString()); } catch (NumberFormatException ignored) {}
            if (params.get("limit") != null) try { s = Integer.parseInt(params.get("limit").toString()); } catch (NumberFormatException ignored) {}
            if (params.get("pageSize") != null) try { s = Integer.parseInt(params.get("pageSize").toString()); } catch (NumberFormatException ignored) {}
        }
        return new Page<>(p, s);
    }
}
