package com.powersmart.worker.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.powersmart.worker.entity.LabourWorktype;
import com.powersmart.worker.mapper.LabourWorktypeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 工种字典 Service — 提取 LabourWorktypeController 的业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LabourWorktypeService {

    private final LabourWorktypeMapper worktypeMapper;

    /**
     * 查询 id + worktypeName + worktypeCode (启用的工种，按 sortOrder 排序)
     */
    public List<Map<String, Object>> selectIdsAndWorktype() {
        List<LabourWorktype> list = worktypeMapper.selectList(
                new LambdaQueryWrapper<LabourWorktype>()
                        .eq(LabourWorktype::getStatus, 1)
                        .orderByAsc(LabourWorktype::getSortOrder));
        return list.stream().map(w -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", w.getId());
            m.put("worktypeName", w.getWorktypeName());
            m.put("worktypeCode", w.getWorktypeCode());
            return m;
        }).collect(Collectors.toList());
    }

    /**
     * 查询 id + name (启用的工种，按 sortOrder 排序)
     */
    public List<Map<String, Object>> selectIdsAndName() {
        List<LabourWorktype> list = worktypeMapper.selectList(
                new LambdaQueryWrapper<LabourWorktype>()
                        .eq(LabourWorktype::getStatus, 1)
                        .orderByAsc(LabourWorktype::getSortOrder));
        return list.stream().map(w -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", w.getId());
            m.put("name", w.getWorktypeName());
            return m;
        }).collect(Collectors.toList());
    }

    /**
     * 查询所有工种（按 sortOrder 排序）
     */
    public List<LabourWorktype> listAll() {
        return worktypeMapper.selectList(
                new LambdaQueryWrapper<LabourWorktype>()
                        .orderByAsc(LabourWorktype::getSortOrder));
    }

    /**
     * 新增
     */
    public void add(LabourWorktype entity) {
        worktypeMapper.insert(entity);
    }

    /**
     * 编辑
     */
    public void update(LabourWorktype entity) {
        worktypeMapper.updateById(entity);
    }

    /**
     * 删除
     */
    public void delete(Long id) {
        worktypeMapper.deleteById(id);
    }
}
