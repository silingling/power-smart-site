package com.powersmart.worker.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.worker.entity.LabourSubcontractor;
import com.powersmart.worker.mapper.LabourSubcontractorMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/build/labourSubcontractor")
@RequiredArgsConstructor
public class LabourSubcontractorController {

    private final LabourSubcontractorMapper mapper;

    @SuppressWarnings("unchecked")
    private Page<LabourSubcontractor> extractPage(Map<String, Object> params) {
        int p = 1, s = 20;
        if (params != null) {
            if (params.get("page") != null) try { p = Integer.parseInt(params.get("page").toString()); } catch (NumberFormatException ignored) {}
            if (params.get("limit") != null) try { s = Integer.parseInt(params.get("limit").toString()); } catch (NumberFormatException ignored) {}
            if (params.get("pageSize") != null) try { s = Integer.parseInt(params.get("pageSize").toString()); } catch (NumberFormatException ignored) {}
        }
        return new Page<>(p, s);
    }

    @PostMapping("/list")
    public Result<PageResult<LabourSubcontractor>> list(@RequestBody(required = false) Map<String, Object> params) {
        Page<LabourSubcontractor> page = extractPage(params);
        LambdaQueryWrapper<LabourSubcontractor> wrapper = buildWrapper(params);
        return Result.ok(PageResult.from(mapper.selectPage(page, wrapper)));
    }

    @PostMapping("/selectPageList")
    public Result<PageResult<LabourSubcontractor>> selectPageList(@RequestBody(required = false) Map<String, Object> params) {
        Page<LabourSubcontractor> page = extractPage(params);
        LambdaQueryWrapper<LabourSubcontractor> wrapper = buildWrapper(params);
        return Result.ok(PageResult.from(mapper.selectPage(page, wrapper)));
    }

    @PostMapping("/queryById/{id}")
    public Result<LabourSubcontractor> queryById(@PathVariable Long id) {
        return Result.ok(mapper.selectById(id));
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody LabourSubcontractor entity) {
        mapper.insert(entity);
        return Result.ok();
    }

    @PostMapping("/edit")
    public Result<Void> edit(@RequestBody LabourSubcontractor entity) {
        mapper.updateById(entity);
        return Result.ok();
    }

    @PostMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        mapper.deleteById(id);
        return Result.ok();
    }

    @PostMapping("/selcetIdsAndName")
    public Result<List<Map<String, Object>>> selcetIdsAndName(@RequestBody(required = false) Map<String, Object> params) {
        LambdaQueryWrapper<LabourSubcontractor> wrapper = new LambdaQueryWrapper<>();
        if (params != null && params.containsKey("projectId") && params.get("projectId") != null)
            wrapper.eq(LabourSubcontractor::getProjectId, Long.valueOf(params.get("projectId").toString()));
        wrapper.eq(LabourSubcontractor::getStatus, 1);
        List<LabourSubcontractor> list = mapper.selectList(wrapper);
        List<Map<String, Object>> result = list.stream().map(s -> {
            Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("id", s.getId());
            m.put("subcontractorName", s.getSubcontractorName());
            return m;
        }).collect(Collectors.toList());
        return Result.ok(result);
    }

    private LambdaQueryWrapper<LabourSubcontractor> buildWrapper(Map<String, Object> params) {
        LambdaQueryWrapper<LabourSubcontractor> wrapper = new LambdaQueryWrapper<>();
        if (params != null) {
            if (params.containsKey("projectId") && params.get("projectId") != null)
                wrapper.eq(LabourSubcontractor::getProjectId, Long.valueOf(params.get("projectId").toString()));
            if (params.containsKey("subcontractorName") && params.get("subcontractorName") != null)
                wrapper.like(LabourSubcontractor::getSubcontractorName, params.get("subcontractorName").toString());
            if (params.containsKey("status") && params.get("status") != null)
                wrapper.eq(LabourSubcontractor::getStatus, Integer.parseInt(params.get("status").toString()));
        }
        wrapper.orderByDesc(LabourSubcontractor::getCreatedAt);
        return wrapper;
    }
}
